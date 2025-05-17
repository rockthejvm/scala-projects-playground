package com.rtjvm.chat.backend

import cask.*
import com.rtjvm.chat.shared.models.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import upickle.default.*

import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Server extends cask.MainRoutes {

  private val database      = new Database("testdb", createDataDir())
  private val wsConnections = ConcurrentHashMap.newKeySet[cask.WsChannelActor]()

  private val mysql = new MySql("chatdb")

  @cask.staticFiles("/static")
  def staticFileRoutes() = "chat-app/js/static"

  @cask.postJson("/chat")
  def postChatMsg(name: String, msg: String): ujson.Obj =
    if name.isEmpty then ujson.Obj("success" -> false, "err" -> "Name cannot be empty")
    else if msg.isEmpty then ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
    else {
      database.saveMsg(Message(name = name, msg = msg))
      val msgs     = database.messages
      val ms       = mysql.messages
      val msgsJson = write(msgs)
      wsConnections.forEach(_.send(cask.Ws.Text(msgsJson)))
      ujson.Obj("success" -> true, "messages" -> writeJs(msgs))
    }

  @cask.websocket("/subscribe")
  def subscribe(): WsHandler = cask.WsHandler { connection =>
    val msgs = write(database.messages)
    connection.send(cask.Ws.Text(msgs))
    wsConnections.add(connection)

    cask.WsActor { case cask.Ws.Close(_, _) =>
      wsConnections.remove(connection)
    }
  }

  @cask.get("/api/greet")
  def greet(name: String): String =
    val token = scala.util.Random.nextInt()
    Greeting(s"Hello $name, from Scala.js backend! $token").asJson.toString

  @cask.get("/")
  def index(): Response[String] =
    Response(
      """|<!DOCTYPE html>
         |<html>
         |  <head>
         |    <script type="module" src="static/main.js"></script>
         |  </head>
         |  <body>
         |    <div id="app">Hello World</div>
         |  </body>
         |</html>""".stripMargin,
      headers = Seq("Content-Type" -> "text/html")
    )

  private def createDataDir(): File =
    val dataDir = os.home / "data"
    os.makeDir.all(dataDir)
    dataDir.toIO

  initialize()
}
