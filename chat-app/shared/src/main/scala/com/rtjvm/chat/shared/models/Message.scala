package com.rtjvm.chat.shared.models

import upickle.default.*

case class Message(sender: String, msg: String)

object Message:
  implicit val rw: ReadWriter[Message] = macroRW
