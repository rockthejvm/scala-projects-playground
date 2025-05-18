package com.rtjvm.chat.shared.models

import upickle.default.*

case class ChatResponse(
    success:  Boolean,
    messages: List[Message],
    err:      Option[String] = None
)

object ChatResponse:
  implicit val rw: ReadWriter[ChatResponse] = macroRW

  def error(err: String): ChatResponse = ChatResponse(err = Some(err), success = false, messages = List.empty)
  def success(messages: List[Message]): ChatResponse = ChatResponse(success = true, messages = messages, err = None)
