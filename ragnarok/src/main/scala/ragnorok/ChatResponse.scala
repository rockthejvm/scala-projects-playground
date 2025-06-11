package ragnorok

import cats.effect.IO
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import io.circe.generic.auto.deriveEncoder

case class ChatResponse(answer: String)

object ChatResponse:
  implicit val encoder: EntityEncoder[IO, ChatResponse] = jsonEncoderOf[IO, ChatResponse]
