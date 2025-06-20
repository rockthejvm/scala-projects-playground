package ragnorok

import cats.effect.IO
import upickle.default.*

case class ChatRequest(question: String)

object ChatRequest:
  implicit val rw: ReadWriter[ChatRequest] = macroRW
