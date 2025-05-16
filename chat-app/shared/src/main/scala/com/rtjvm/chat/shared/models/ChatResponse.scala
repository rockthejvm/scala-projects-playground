package com.rtjvm.chat.shared.models

import upickle.default.*

case class ChatResponse(
  success:  Boolean,
  messages: List[Message],
  err:      Option[String] = None
)

object ChatResponse:
  implicit val rw: ReadWriter[ChatResponse] = macroRW
