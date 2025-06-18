package ragnorok

import upickle.default.*

case class ChatStreamResponse(content: String, references: List[String] = Nil)

object ChatStreamResponse:
  implicit val rw: ReadWriter[ChatStreamResponse] = macroRW

  def toEventString(response: ChatStreamResponse): String =
    s"data: ${write(response)}\n\n"
