package com.rtjvm.chat.backend

import scalasql.*

/** Represents a row in msg table in the database
  */
case class Msg[T[_]](sender: T[String], msg: T[String])

object Msg extends Table[Msg]()
