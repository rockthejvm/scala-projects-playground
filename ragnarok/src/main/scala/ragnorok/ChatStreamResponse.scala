package ragnorok

import io.circe.syntax.*
import io.circe.generic.auto.*
import upickle.default.*

case class ChatStreamResponse(content: String, references: List[String] = Nil)

object ChatStreamResponse:
  implicit val rw: ReadWriter[ChatStreamResponse] = macroRW

  def toEventString(response: ChatStreamResponse): String =
    s"data: ${response.asJson.noSpaces}\n\n"
