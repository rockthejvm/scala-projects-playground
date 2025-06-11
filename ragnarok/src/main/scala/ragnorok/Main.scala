package ragnorok

import cats.effect.*
import com.comcast.ip4s.*
import io.circe.generic.auto.deriveEncoder
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger as HttpLogger
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends IOApp.Simple {
  private val logger = LoggerFactory[IO].getLogger

  private val httpApp = Router(
    "/"    -> HealthService.routes,
    "/api" -> ChatService.routes(using logger)
  ).orNotFound

  private val httpAppWithLogging = HttpLogger.httpApp(
    logHeaders = true,
    logBody    = true
  )(httpApp)

  private val serverConfig: Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"localhost") // Using localhost for development
      .withPort(port"8080")
      .withHttpApp(httpAppWithLogging)
      .withoutHttp2
      .build

  def run: IO[Unit] = {
    for {
      _ <- logger.info("=== Starting Ragnarok Server ===")
      _ <- logger.info("Registered routes:")
      _ <- logger.info("  GET  /            - Root endpoint")
      _ <- logger.info("  GET  /health      - Health check endpoint")
      _ <- logger.info("  POST /api/chat    - Chat endpoint")

      _ <- serverConfig.use { server =>
        val port = server.address.getPort
        for {
          _ <- logger.info("\n=== Server Started Successfully ===")
          _ <- logger.info(s"Server running at:")
          _ <- logger.info(s"  - http://localhost:$port")
          _ <- logger.info("\nPress Ctrl+C to stop the server...")
          _ <- IO.never
        } yield ()
      }
    } yield ()
  }.onError { e =>
    logger.error(e)("Server failed to start") *> IO.raiseError(e)
  }
}
