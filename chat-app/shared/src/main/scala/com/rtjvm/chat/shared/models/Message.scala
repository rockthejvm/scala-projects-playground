package com.rtjvm.chat.shared.models

import upickle.default.*

import java.time.{LocalDateTime, ZoneOffset}

case class Message(id: Long, sender: String, msg: String, timestamp: Long)

object Message:
  implicit val rw: ReadWriter[Message] = macroRW

  def apply(id: Long, sender: String, msg: String, timestamp: LocalDateTime): Message =
    new Message(id, sender, msg, timestamp.toInstant(ZoneOffset.UTC).toEpochMilli)
