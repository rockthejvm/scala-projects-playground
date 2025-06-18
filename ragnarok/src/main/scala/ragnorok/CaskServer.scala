package ragnorok

import cask.*
import cask.model.Response
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.langchain4j.rag.query.Query
import org.typelevel.log4cats.LoggerFactory
import upickle.default.*

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

object CaskServer extends cask.MainRoutes {
  private val logger        = LoggerFactory[IO].getLogger
  private val wsConnections = new ConcurrentHashMap[String, WsChannelActor]()

  @cask.websocket("/subscribe/:clientName")
  def subscribe(clientName: String): WsHandler = cask.WsHandler { connection =>
    val validClientName = !clientName.isBlank && !wsConnections.containsKey(clientName)

    if validClientName then wsConnections.put(clientName, connection)

    cask.WsActor { case cask.Ws.Close(_, _) =>
      wsConnections.remove(connection)
    }
  }

  @cask.get("/")
  def rootRedirect(): Response[String] = {
    // Response("OK", statusCode = 302, headers = Seq("Location" -> "/index.html"))
    Response("OK", statusCode = 200)
  }

  @cask.staticFiles("/static")
  def staticFileRoutes(): String =
    val userDir    = System.getProperty("user.dir")
    val staticPath = os.Path(userDir) / "ragnarok" / "src" / "main" / "resources" / "static"

    println(s"staticPath: $userDir")

    if os.exists(staticPath) then staticPath.toString  // when running from IDE
    else (os.Path(userDir) / ".." / "static").toString // when running from ragnarok folder on the command line

  @cask.postJson("/:clientName/chat")
  def chat(clientName: String, question: String): Response[String] =
    if clientName.isBlank || !wsConnections.containsKey(clientName) then Response("No such client", statusCode = 400)
    else {
      val clientName = "qwerty"
      val contents   = queryContext(question)

      ChatService.assistant
        .chat(question, contents)
        .onPartialResponse { token =>
          val json = writeJs(Map("content" -> token)).render()
          val data = s"data: $json\n\n"
          wsConnections.get(clientName).send(cask.Ws.Text(data))
        }
        .onCompleteResponse { _ =>
          val data = cask.Ws.Text("data: [DONE]\n\n")
          wsConnections.get(clientName).send(data)
        }
        .onError { error =>
          val data = cask.Ws.Text(s"event: error\ndata: ${error.getMessage}\n\n")
          wsConnections.get(clientName).send(data)
        }
        .start()

      Response("OK", statusCode = 202)
    }

  private def queryContext(question: String) = {
    val query           = Query.from(question)
    val relevantContent = Logic.contentRetriever.retrieve(query).asScala.toList

    val references: List[String] =
      relevantContent.flatMap { content =>
        Option(
          content
            .textSegment()
            .metadata()
            .getString("file_name")
        ).filterNot(_.isBlank)
      }.distinct

    logger.info(s"Found references: ${references.mkString(", ")}").unsafeRunAndForget()

    relevantContent
  }

  initialize()

  Logic.ingestDocuments()
}
