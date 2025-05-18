package com.rtjvm.chat.shared.models

import upickle.default.*

case class Message(sender: String, msg: String, timestamp: Long)

object Message:
  implicit val rw: ReadWriter[Message] = macroRW

  def apply(sender: String, msg: String): Message = Message(sender, msg, System.currentTimeMillis)
