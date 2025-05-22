package com.rtjvm.chat.backend

import scalasql.*

import java.time.LocalDateTime

/** Represents a row in msg table in the database
  */
case class Msg[T[_]](id: T[Long], sender: T[String], msg: T[String], parent: T[Option[Long]], sentTs: T[LocalDateTime])

object Msg extends Table[Msg]()
