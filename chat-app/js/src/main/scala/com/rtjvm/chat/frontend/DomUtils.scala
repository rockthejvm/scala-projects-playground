package com.rtjvm.chat.frontend

import org.scalajs.dom

object DomUtils {
  def getByIdAs[T <: dom.Element](id: String): T =
    val e = dom.document.getElementById(id)
    if e != null then e.asInstanceOf[T]
    else null.asInstanceOf[T]
}
