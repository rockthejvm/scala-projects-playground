package ragnorok

import cats.effect.IO
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import io.circe.generic.auto.deriveDecoder

case class ChatRequest(question: String)

object ChatRequest:
  implicit val decoder: EntityDecoder[IO, ChatRequest] = jsonOf[IO, ChatRequest]
