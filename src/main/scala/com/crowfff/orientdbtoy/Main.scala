package com.crowfff.orientdbtoy

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      config <- AppConfig.load[IO]
      host <- IO.fromOption(Host.fromString(config.host))(new IllegalArgumentException(s"Invalid host: ${config.host}"))
      port <- IO.fromOption(Port.fromInt(config.port))(new IllegalArgumentException(s"Invalid port: ${config.port}"))
      exitCode <- OrientDbEntityStore
        .resource[IO](config)
        .flatMap { store =>
          EmberServerBuilder
            .default[IO]
            .withHost(host)
            .withPort(port)
            .withHttpWebSocketApp { wsBuilder =>
              Router("/" -> new EntityRoutes[IO](store).routes(wsBuilder)).orNotFound
            }
            .build
        }
        .useForever
        .as(ExitCode.Success)
    } yield exitCode
}
