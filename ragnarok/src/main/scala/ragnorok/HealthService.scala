package ragnorok

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.HttpRoutes
import org.typelevel.log4cats.LoggerFactory

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale

object HealthService:
  private val logger = LoggerFactory[IO].getLogger

  private val formatter = DateTimeFormatter
    .ofPattern("EEE, MMM dd, yyyy hh:mm a SSS 'ms'", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

  val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "health" =>
        val now       = ZonedDateTime.now()
        val formatted = now.format(formatter) // DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        Ok(s"Service is up ($formatted)")

      case req @ POST -> Root / "echo" =>
        for {
          _    <- logger.info("Received (echo) request")
          body <- req.as[String]
          _    <- logger.info(s"Request body: $body")
          resp <- Ok(s"Received: $body")
        } yield resp

      case GET -> Root =>
        Ok("Ragnarok API is running")
    }
