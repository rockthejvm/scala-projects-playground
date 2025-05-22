package com.rtjvm.chat.shared.models

import upickle.default.*

import java.time.{LocalDateTime, ZoneOffset}

case class NewMessage(sender: String, msg: String, parent: Option[Long], timestamp: Long)

object NewMessage:
  implicit val rw: ReadWriter[NewMessage] = macroRW

  def apply(sender: String, msg: String, parent: Option[Long] = None): NewMessage =
    new NewMessage(sender, msg, parent, System.currentTimeMillis)
