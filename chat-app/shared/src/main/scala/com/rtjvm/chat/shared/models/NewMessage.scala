package com.rtjvm.chat.shared.models

import upickle.default.*

import java.time.{LocalDateTime, ZoneOffset}

case class NewMessage(sender: String, msg: String, timestamp: Long)

object NewMessage:
  implicit val rw: ReadWriter[NewMessage] = macroRW

  def apply(sender: String, msg: String): NewMessage =
    NewMessage(sender, msg, System.currentTimeMillis)
