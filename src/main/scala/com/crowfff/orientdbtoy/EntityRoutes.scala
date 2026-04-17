package com.crowfff.orientdbtoy

import cats.effect.kernel.Async
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.{HttpRoutes, Response}
import org.http4s.StaticFile
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

final class EntityRoutes[F[_]: Async](entityStore: EntityStore[F]) extends Http4sDsl[F] {
  private val staticRoutes = HttpRoutes.of[F] {
    case request @ GET -> Root =>
      StaticFile
        .fromResource("/public/index.html", Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "app.js" =>
      StaticFile
        .fromResource("/public/app.js", Some(request))
        .map(_.putHeaders(`Content-Type`(org.http4s.MediaType.text.javascript)))
        .getOrElseF(NotFound())
  }

  def routes(wsBuilder: WebSocketBuilder2[F]): HttpRoutes[F] = {
    val apiRoutes = HttpRoutes.of[F] {
      case req @ POST -> Root / "entity" =>
        for {
          entity <- req.as[EntityRequest]
          _ <- entityStore.add(entity.id)
          response <- Created()
        } yield response

      case DELETE -> Root / "entity" / IntVar(id) =>
        entityStore.remove(id) *> NoContent()

      case GET -> Root / "entity" =>
        websocketResponse(wsBuilder)
    }

    staticRoutes <+> apiRoutes
  }

  private def websocketResponse(wsBuilder: WebSocketBuilder2[F]): F[Response[F]] =
    wsBuilder.build(
      send = entityStore.events.map(event => WebSocketFrame.Text(event.asJson.noSpaces)),
      receive = _.drain
    )
}
