package com.rtjvm.chat.frontend

import com.rtjvm.chat.shared.models.Message
import org.scalajs.dom
import org.scalajs.dom.html.Div
import scalatags.JsDom
import scalatags.JsDom.all.*

import scala.scalajs.js.Date

object DomUtils {
  def getByIdAs[T <: dom.Element](id: String): T =
    val e = dom.document.getElementById(id)
    if e != null then e.asInstanceOf[T]
    else null.asInstanceOf[T]

  def fragFor(m: Message): JsDom.TypedTag[Div] =
    div(cls := "message")(
      span(cls := "sender")(s"${m.sender}:"),
      span(cls := "msg")(m.msg),
      span(cls := "timestamp")(new Date(m.timestamp).toISOString())
    )
}
