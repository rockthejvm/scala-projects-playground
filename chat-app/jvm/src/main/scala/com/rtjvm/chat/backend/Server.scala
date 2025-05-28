package com.rtjvm.chat.backend

import cask.*
import com.rtjvm.chat.shared.models.*
import scalatags.Text.all.*
import upickle.default.*

import java.util.concurrent.ConcurrentHashMap

object Server extends cask.MainRoutes {

  private val wsConnections = ConcurrentHashMap.newKeySet[cask.WsChannelActor]()
  private val postgres      = new Postgres(createDataDir(), "chatdb", 5432)
  private val listener      = PostgresListener()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      println("JVM is shutting down. Stopping listener...")
      listener.stopListening() // Ensure the listener is stopped and the connection is closed
    }
  })

  listener.startListening()

  @cask.getJson("/messages")
  def queryAllMessages(): Seq[Message] = {
    postgres.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs, m.parent))
  }

  @cask.getJson("/messages/:searchTerm")
  def queryMessages(searchTerm: String): Seq[Message] = {
    postgres.messages(searchTerm).map(m => Message(m.id, m.sender, m.msg, m.sentTs, m.parent))
  }

  @cask.postJson("/chat")
  def postChatMsg(
      sender:    String,
      msg:       String,
      parent:    Option[Long] = None,
      timestamp: Option[Long] = None
  ): ujson.Value =
    (sender.trim, msg.trim) match
      case ("", _) => writeJs(ChatResponse.error("Name cannot be empty"))
      case (_, "") => writeJs(ChatResponse.error("Message cannot be empty"))
      case (sender, msg) =>
        postgres.saveMsg(NewMessage(sender, msg, parent.filter(_ > 0)))

        val msgs    = postgres.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs, m.parent))
        val payload = cask.Ws.Text(write(msgs))

        wsConnections.forEach(_.send(payload))
        writeJs(ChatResponse.success(msgs.toList))

  @cask.websocket("/subscribe")
  def subscribe(): WsHandler = cask.WsHandler { connection =>
    val ms = postgres.messages.map(m => Message(m.id, m.sender, m.msg, m.sentTs, m.parent))

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

  private def createDataDir(): String =
    val dataDir = os.home / "pgdata"
    os.makeDir.all(dataDir)
    dataDir.toString

  initialize()
}
