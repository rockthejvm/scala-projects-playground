package com.rtjvm.chat.backend

import cask.*
import com.rtjvm.chat.shared.models.*
import scalatags.Text.all.*
import upickle.default.*

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Server extends cask.MainRoutes {

  private val wsConnections = ConcurrentHashMap.newKeySet[cask.WsChannelActor]()
  private val mysql         = new MySql("chatdb", createDataDir())
  private val postgres      = new Database("chatdb", "postgres", 5432)

  @cask.getJson("/messages")
  def queryAllMessages(): Seq[Message] = {
    postgres.messages.foreach(println)
    mysql.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs))
  }

  @cask.getJson("/messages/:searchTerm")
  def queryMessages(searchTerm: String): Seq[Message] = {
    mysql.messages(searchTerm).map(m => Message(m.id, m.sender, m.msg, m.sentTs))
  }

  @cask.postJson("/chat")
  def postChatMsg(sender: String, msg: String, timestamp: Option[Long] = None): ujson.Value =
    (sender.trim, msg.trim) match
      case ("", _) => writeJs(ChatResponse.error("Name cannot be empty"))
      case (_, "") => writeJs(ChatResponse.error("Message cannot be empty"))
      case (sender, msg) =>
        mysql.saveMsg(sender, msg, timestamp.getOrElse(System.currentTimeMillis))
        val msgs    = mysql.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs))
        val payload = cask.Ws.Text(write(msgs))
        wsConnections.forEach(_.send(payload))
        writeJs(ChatResponse.success(msgs.toList))

  @cask.websocket("/subscribe")
  def subscribe(): WsHandler = cask.WsHandler { connection =>
    val ms = mysql.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs))

    connection.send(cask.Ws.Text(write(ms)))
    wsConnections.add(connection)

    cask.WsActor { case cask.Ws.Close(_, _) =>
      wsConnections.remove(connection)
    }
  }

  @cask.get("/")
  def index(): doctype =
    doctype("html")(
      html(
        head(
          title := "Hello World",
          link(rel := "stylesheet", href := "static/main.css")
        ),
        body(
          h1("Hello World"),
          p(Utils.token)
        )
      )
    )

  @cask.get("/api/greet")
  def greet(name: String): String =
    val token = scala.util.Random.nextInt()
    write(Greeting(s"Hello $name, from Scala.js backend! $token"))

  @cask.staticFiles("/static")
  def staticFileRoutes() = "chat-app/js/static"

  private def createDataDir(): File =
    val dataDir = os.home / "data"
    os.makeDir.all(dataDir)
    dataDir.toIO

  initialize()
}
