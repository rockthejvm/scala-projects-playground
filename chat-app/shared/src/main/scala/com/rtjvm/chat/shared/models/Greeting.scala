package com.rtjvm.chat.shared.models

import upickle.default.*

case class Greeting(message: String)

object Greeting:
  implicit val rw: ReadWriter[Greeting] = macroRW
