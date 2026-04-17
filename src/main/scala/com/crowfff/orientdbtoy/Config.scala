package com.crowfff.orientdbtoy

import cats.effect.kernel.Sync

import scala.util.Try

final case class AppConfig(
    orientDbUrl: String,
    serverUser: String,
    serverPassword: String,
    database: String,
    dbUser: String,
    dbPassword: String,
    host: String,
    port: Int
)

object AppConfig {
  def load[F[_]: Sync]: F[AppConfig] =
    Sync[F].delay {
      def envOrElse(name: String, default: String): String =
        sys.env.get(name).filter(_.nonEmpty).getOrElse(default)

      AppConfig(
        orientDbUrl = envOrElse("ORIENTDB_URL", "remote:localhost"),
        serverUser = envOrElse("ORIENTDB_SERVER_USER", "root"),
        serverPassword = envOrElse("ORIENTDB_SERVER_PASSWORD", "rootpwd"),
        database = envOrElse("ORIENTDB_DATABASE", "toy"),
        dbUser = envOrElse("ORIENTDB_DB_USER", "root"),
        dbPassword = envOrElse("ORIENTDB_DB_PASSWORD", "rootpwd"),
        host = envOrElse("HTTP_HOST", "0.0.0.0"),
        port = Try(envOrElse("HTTP_PORT", "8080").toInt).getOrElse {
          throw new IllegalArgumentException("HTTP_PORT must be a valid integer")
        }
      )
    }
}
