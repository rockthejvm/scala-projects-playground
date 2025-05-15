package chat.backend

import cask.endpoints.WsHandler
import chat.backend.model.Message
import io.getquill.*
import scalatags.Text.all.*

import java.util.concurrent.ConcurrentHashMap

object MinApp extends cask.MainRoutes:
  private final val BootstrapCss = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"

  private val dataDir = os.home / "data"
  os.makeDir.all(dataDir)
  private val database: ch.vorburger.mariadb4j.DB   = Database.startMySQL(dataDir.toIO)
  private val ctx:      MysqlJdbcContext[LowerCase] = Database.initDb(database, "testdb")

  import ctx.*

  private def messages = ctx.run(query[Message].map(m => (m.name, m.msg)))

  private val wsConnections = ConcurrentHashMap.newKeySet[cask.WsChannelActor]()

  @cask.staticResources("/static")
  def staticResources() = "static"

  @cask.get("/")
  def hello(): doctype = {
    doctype("html")(
      html(
        head(
          link(rel   := "stylesheet", href := BootstrapCss),
          script(src := "/static/app.js")
        ),
        body(
          div(cls := "container")(
            h1("Scala Chat!"),
            div(id := "messageList")(messageList()),
            div(id := "errorDiv", color.red),
            form(
              onsubmit := "submitForm(); return false"
            )(
              input(`type` := "text", id := "nameInput", placeholder := "User name"),
              input(`type` := "text", id := "msgInput", placeholder  := "Write a message!"),
              input(`type` := "submit")
            )
          )
        )
      )
    )
  }

  @cask.postJson("/")
  def postChatMsg(name: String, msg: String): ujson.Obj =
    if name.isEmpty then ujson.Obj("success" -> false, "err" -> "Name cannot be empty")
    else if msg.isEmpty then ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
    else {
      ctx.run(query[Message].insertValue(lift(Message(name = name, msg = msg))))
      wsConnections.forEach(_.send(cask.Ws.Text(messageList().render)))
      ujson.Obj("success" -> true, "txt" -> messageList().render, "err" -> "")
    }

  @cask.websocket("/subscribe")
  def subscribe(): WsHandler = cask.WsHandler { connection =>
    connection.send(cask.Ws.Text(messageList().render))
    wsConnections.add(connection)

    cask.WsActor { case cask.Ws.Close(_, _) =>
      wsConnections.remove(connection)
    }
  }

  @cask.post("/do-thing")
  def doThing(request: cask.Request): String = {
    request.text().reverse
  }

  private def messageList() =
    frag(messages.map((name, msg) => p(b(name), ": ", msg)))

  initialize()
end MinApp
