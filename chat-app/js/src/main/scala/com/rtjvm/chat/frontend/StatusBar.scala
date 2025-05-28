package com.rtjvm.chat.frontend

import org.scalajs.dom

class StatusBar(val el: dom.html.Element) {
  def setError(msg: String): Unit = {
    el.textContent = msg
    el.style.color = "red"
  }

  def setInfo(msg: String): Unit = {
    el.textContent = msg
    el.style.color = "black"
  }

  def clear(): Unit = {
    el.textContent = ""
    el.style.color = "black"
  }
}
