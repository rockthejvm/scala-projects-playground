package com.rtjvm.chat.shared.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Greeting(message: String)

object Greeting {
  implicit val decoder: Decoder[Greeting] = deriveDecoder[Greeting]
  implicit val encoder: Encoder[Greeting] = deriveEncoder[Greeting]
}
