package com.rtjvm.chat.backend

import scalasql.*

case class Msg[T[_]](sender: T[String], msg: T[String])
object Msg extends Table[Msg]()
