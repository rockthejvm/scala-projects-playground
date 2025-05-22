package com.rtjvm.chat.shared.models

import upickle.default.*

import java.time.{LocalDateTime, ZoneOffset}

case class Message(id: Long, sender: String, msg: String, parent: Option[Long], timestamp: Long)

object Message:
  implicit val rw: ReadWriter[Message] = macroRW

  def apply(id: Long, sender: String, msg: String, timestamp: LocalDateTime, parent: Option[Long] = None): Message =
    new Message(id, sender, msg, parent, timestamp.toInstant(ZoneOffset.UTC).toEpochMilli)
