package ragnorok

import io.circe.syntax.*
import io.circe.generic.auto.*

case class ChatStreamResponse(content: String, references: List[String] = Nil)

object ChatStreamResponse:
  def toEventString(response: ChatStreamResponse): String =
    s"data: ${response.asJson.noSpaces}\n\n"
