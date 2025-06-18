package ragnorok

import cats.effect.IO
import upickle.default.*

case class ChatResponse(answer: String)

object ChatResponse:
  implicit val rw: ReadWriter[ChatResponse] = macroRW
