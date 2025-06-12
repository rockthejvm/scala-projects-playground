package ragnorok

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.headers.Location
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger as HttpLogger
import org.http4s.server.staticcontent.*
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends IOApp.Simple {
  private val logger = LoggerFactory[IO].getLogger

  def run: IO[Unit] = {
    for {
      _ <- logger.info("=== Starting Ragnarok Server ===")
      _ <- logger.info("Registered routes:")
      _ <- logger.info("  GET  /            - Redirects to /static/index.html")
      _ <- logger.info("  GET  /static/*     - Static file server")
      _ <- logger.info("  GET  /health      - Health check endpoint")
      _ <- logger.info("  POST /api/chat    - Chat endpoint")

      _ <- serverConfig.use { server =>
        for {
          _ <- logger.info("\n=== Server Started Successfully ===")
          _ <- logger.info(s"Server running at:")
          _ <- logger.info(s"  - http://localhost:${server.address.getPort}")
          _ <- logger.info("\nPress Ctrl+C to stop the server...")
          _ <- IO.never
        } yield ()
      }
    } yield ()
  }.onError { e =>
    logger.error(e)("Server failed to start") *> IO.raiseError(e)
  }

  private val rootRedirect = HttpRoutes.of[IO] { case GET -> Root =>
    Response[IO](status = Status.TemporaryRedirect)
      .withHeaders(Location(uri"/static/index.html"))
      .pure[IO]
  }

  private val httpAppWithLogging = //HttpLogger.httpApp(logHeaders = true, logBody = true)(
    Router(
      "/"       -> rootRedirect,
      "/static" -> resourceServiceBuilder[IO]("static").toRoutes,
      "/api"    -> (ChatService.routes(using logger) <+> HealthService.routes)
    ).orNotFound
  //)

  private val serverConfig: Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"localhost") // Using localhost for development
      .withPort(port"8080")
      .withHttpApp(httpAppWithLogging)
      .withoutHttp2
      .build
}
