package com.rtjvm.chat.frontend

import com.rtjvm.chat.frontend.DomUtils.*
import com.rtjvm.chat.shared.models.*
import org.scalajs.dom
import org.scalajs.dom.html.Div
import org.scalajs.dom.{HttpMethod, WebSocket, html}
import scalatags.JsDom
import scalatags.JsDom.all._
import upickle.default.*

import scala.scalajs.js

object Main extends App {
  private val ApiServer    = "http://localhost:8080"
  private val chatForm     = getByIdAs[html.Form]("chat-form")
  private val senderInput  = getByIdAs[html.Input]("sender")
  private val msgInput     = getByIdAs[html.Input]("message")
  private val replyToInput = getByIdAs[html.Input]("reply-to")
  private val sendBtn      = getByIdAs[html.Button]("send")
  private val messagesDiv  = getByIdAs[html.Div]("messages")
  private val errorDiv     = dom.document.getElementById("error")

  assert(chatForm != null, "Chat form not found")
  assert(senderInput != null, "Name input not found")
  assert(msgInput != null, "Message input not found")
  assert(replyToInput != null, "Reply-To input not found")
  assert(sendBtn != null, "Greet button not found")
  assert(errorDiv != null, "Greeting div not found")

  private val statusBar = new StatusBar(errorDiv.asInstanceOf[dom.html.Element])
  private val socket    = new WebSocket("ws://localhost:8080/subscribe")

  dom.document
    .getElementById("search-input")
    .addEventListener(
      "keydown",
      (event: dom.KeyboardEvent) =>
        if (event.key == "Enter") {
          val searchInput = event.target.asInstanceOf[dom.HTMLInputElement]
          val searchTerm  = searchInput.value.trim

          val requestInit = new dom.RequestInit {
            method  = HttpMethod.GET
            headers = js.Dictionary("Content-Type" -> "application/json")
          }

          dom
            .fetch(s"$ApiServer/messages/$searchTerm", requestInit)
            .`then` { response =>
              response
                .text()
                .`then` { text =>
                  try {
                    val messages = read[Seq[Message]](text)
                    println(s"Received messages: $messages")
                    if searchTerm.isBlank then renderMessages(messages)
                    else renderFilteredMsgs(messages)
                  } catch {
                    case e: Exception =>
                      System.err.println(s"Error parsing response: ${e.getMessage}")
                      statusBar.setError("Error sending message")
                  }
                }
            }
        }
    )

  sendBtn.addEventListener(
    "click",
    _ => {
      if (senderInput.value.trim.isEmpty) {
        statusBar.setError("Please enter your name")
        senderInput.focus()
      } else if (msgInput.value.trim.isEmpty) {
        statusBar.setError("Please enter a message")
        msgInput.focus()
      } else {
        statusBar.clear()
        val message =
          NewMessage(
            sender = senderInput.value,
            msg    = msgInput.value,
            parent = replyToInput.value.toLongOption
          )

        val requestInit = new dom.RequestInit {
          method  = HttpMethod.POST
          body    = writeJs(message).toString()
          headers = js.Dictionary("Content-Type" -> "application/json")
        }

        dom
          .fetch(s"$ApiServer/chat", requestInit)
          .`then` { response =>
            response
              .text()
              .`then` { (text: String) =>
                try {
                  val messages = read[ChatResponse](text).messages
                  renderMessages(messages)

                  msgInput.value = ""
                  msgInput.focus()
                } catch {
                  case e: Exception =>
                    System.err.println(s"Error parsing response: ${e.getMessage}")
                    statusBar.setError("Error sending message")
                }
              }
          }
          .`catch` { err =>
            System.err.println(s"Error sending message: $err")
            statusBar.setError("Error sending message")
          }
      }
    }
  )

  socket.onopen = { (event: dom.Event) =>
    try {
      println("WebSocket connection established")
      statusBar.setInfo("Connected to chat server")
    } catch {
      case e: Exception =>
        System.err.println(s"Error processing WebSocket message: $e")
        statusBar.setError(s"Error: ${e.getMessage}")
    }
  }

  socket.onmessage = { (event: dom.MessageEvent) =>
    try {
      val messages = read[Seq[Message]](event.data.toString)
      renderMessages(messages)
    } catch {
      case e: Exception =>
        System.err.println(s"Error processing WebSocket message: ${e.getMessage}")
        statusBar.setError(s"Error: ${e.getMessage}")
    }
  }

  socket.onerror = { (event: dom.Event) =>
    println(s"WebSocket error: ${event.toString}")
  }

  private def renderMessages(messages: Seq[Message]): Unit = {
    messagesDiv.innerHTML = messageList(messages).map(_.toString).mkString
  }

  private def messageList(messages: Seq[Message]) = {
    val msgMap = messages.groupBy(_.parent)

    def messageListFrag(parent: Option[Long] = None): Seq[JsDom.TypedTag[Div]] =
      for (msg <- msgMap.getOrElse(parent, Nil))
        yield div(
          fragFor(msg),
          div(paddingLeft := 15)(messageListFrag(Some(msg.id)))
        )

    messageListFrag(None)
  }

  private def renderFilteredMsgs(messages: Seq[Message]): Unit = {
    def formatMsgs: Seq[JsDom.TypedTag[Div]] =
      for (msg <- messages) yield fragFor(msg)

    messagesDiv.innerHTML = formatMsgs.map(_.toString).mkString
  }

  println("Hello from Scala.js frontend!")
}
