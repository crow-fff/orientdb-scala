package com.crowfff.orientdbtoy

import cats.effect.kernel.{Async, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all._
import com.orientechnologies.common.exception.OException
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.db.{ODatabaseSession, ODatabaseType, OLiveQueryMonitor, OLiveQueryResultListener, OrientDB, OrientDBConfig}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.sql.executor.OResult
import fs2.Stream
import fs2.concurrent.Topic

import scala.util.Try

final class OrientDbEntityStore[F[_]: Async] private (
    db: ODatabaseSession,
    topic: Topic[F, Option[EntityEvent]]
) extends EntityStore[F] {

  override def add(id: Int): F[Unit] =
    Async[F].blocking {
      db.activateOnCurrentThread()
      val resultSet = db.command("UPDATE Entity SET id = ? UPSERT WHERE id = ?", Int.box(id), Int.box(id))
      try ()
      finally resultSet.close()
    }

  override def remove(id: Int): F[Unit] =
    Async[F].blocking {
      db.activateOnCurrentThread()
      val resultSet = db.command("DELETE FROM Entity WHERE id = ?", Int.box(id))
      try ()
      finally resultSet.close()
    }

  override val events: Stream[F, EntityEvent] =
    topic.subscribe(1024).unNone
}

object OrientDbEntityStore {
  def resource[F[_]: Async](config: AppConfig): Resource[F, EntityStore[F]] =
    for {
      dispatcher <- Dispatcher.parallel[F]
      orient <- Resource.make {
        Async[F].blocking {
          new OrientDB(config.orientDbUrl, config.serverUser, config.serverPassword, OrientDBConfig.defaultConfig())
        }
      }(db => Async[F].blocking(db.close()).handleErrorWith(_ => Async[F].unit))
      _ <- Resource.eval(Async[F].blocking(orient.createIfNotExists(config.database, ODatabaseType.PLOCAL)))
      session <- Resource.make {
        Async[F].blocking(orient.open(config.database, config.dbUser, config.dbPassword))
      }(db => Async[F].blocking(db.close()).handleErrorWith(_ => Async[F].unit))
      _ <- Resource.eval(ensureSchema(session))
      topic <- Resource.eval(Topic[F, Option[EntityEvent]])
      _ <- startLiveQuery(session, topic, dispatcher)
    } yield {
      new OrientDbEntityStore[F](session, topic)
    }

  private def ensureSchema[F[_]: Async](db: ODatabaseSession): F[Unit] =
    Async[F].blocking {
      db.activateOnCurrentThread()
      val schema = db.getMetadata.getSchema
      val entityClass =
        if (schema.existsClass("Entity")) schema.getClass("Entity")
        else schema.createClass("Entity")

      if (entityClass.getProperty("id") == null) {
        entityClass.createProperty("id", OType.INTEGER)
      }

      if (entityClass.getClassIndex("Entity.id") == null) {
        entityClass.createIndex("Entity.id", OClass.INDEX_TYPE.UNIQUE, "id")
      }
    }

  private def startLiveQuery[F[_]: Async](
      db: ODatabaseSession,
      topic: Topic[F, Option[EntityEvent]],
      dispatcher: Dispatcher[F]
  ): Resource[F, OLiveQueryMonitor] =
    Resource.make {
      Async[F].blocking {
        db.activateOnCurrentThread()
        val listener = new OLiveQueryResultListener {
          override def onCreate(database: ODatabaseDocument, result: OResult): Unit =
            publish(result, EntityEvent.Add.apply)

          override def onUpdate(database: ODatabaseDocument, before: OResult, after: OResult): Unit =
            publish(after, EntityEvent.Add.apply)

          override def onDelete(database: ODatabaseDocument, result: OResult): Unit =
            publish(result, EntityEvent.Remove.apply)

          override def onError(database: ODatabaseDocument, exception: OException): Unit =
            ()

          override def onEnd(database: ODatabaseDocument): Unit =
            ()

          private def publish(result: OResult, mkEvent: Int => EntityEvent): Unit =
            extractId(result).foreach { id =>
              dispatcher.unsafeRunAndForget(topic.publish1(Some(mkEvent(id))))
            }

          private def extractId(result: OResult): Option[Int] =
            Option(result.getProperty[Any]("id")).flatMap {
              case i: java.lang.Integer => Some(i.intValue)
              case l: java.lang.Long if l >= Int.MinValue && l <= Int.MaxValue => Some(l.intValue)
              case s: String            => Try(s.toInt).toOption
              case _                    => None
            }
        }

        db.live("SELECT FROM Entity", listener)
      }
    } { monitor =>
      Async[F].blocking(monitor.unSubscribe()).handleErrorWith(_ => Async[F].unit)
    }
}
