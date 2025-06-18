package ragnorok

import cask.*
import cask.model.Response
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.langchain4j.rag.query.Query
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import upickle.default.*

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object CaskServer extends cask.MainRoutes {
  private val logger        = LoggerFactory[IO].getLogger
  private val wsConnections = new ConcurrentHashMap[String, WsChannelActor]()

  private val formatter = DateTimeFormatter
    .ofPattern("EEE, MMM dd, yyyy hh:mm a SSS 'ms'", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

  @cask.websocket("/subscribe/:clientName")
  def subscribe(clientName: String): WsHandler = cask.WsHandler { connection =>
    val validClientName = !clientName.isBlank && !wsConnections.containsKey(clientName)

    if validClientName then
      logger.info(s"New client connected: $clientName").unsafeRunAndForget()
      wsConnections.put(clientName, connection)

    cask.WsActor { case cask.Ws.Close(_, _) =>
      wsConnections.remove(connection)
    }
  }

  @cask.get("/")
  def rootRedirect(): Response[String] = {
    // Response("OK", statusCode = 302, headers = Seq("Location" -> "/index.html"))
    Response("OK", statusCode = 200)
  }

  @cask.get("/health")
  def health(): Response[String] =
    val now       = ZonedDateTime.now()
    val formatted = now.format(formatter) // DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    Response(s"Service is up ($formatted)", statusCode = 200)

  @cask.staticFiles("/static")
  def staticFileRoutes(): String =
    val userDir    = System.getProperty("user.dir")
    val staticPath = os.Path(userDir) / "ragnarok" / "src" / "main" / "resources" / "static"

    if os.exists(staticPath) then staticPath.toString // when running from IDE
    else
      // when running from ragnarok folder on the command line
      (os.Path(userDir) / "src" / "main" / "resources" / "static").toString

  @cask.postJson("/:clientName/chat")
  def chat(clientName: String, question: String): Response[String] =
    if clientName.isBlank || !wsConnections.containsKey(clientName) then Response("No such client", statusCode = 400)
    else {
      val contents = queryContext(question, wsConnections.get(clientName))

      Logic.assistant
        .chat(question, contents)
        .onPartialResponse { token =>
          // val json = writeJs(Map("content" -> token)).render()
          val json = writeJs(ChatStreamResponse(token)).render()
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

  private def queryContext(question: String, actor: WsChannelActor) = {
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

    val refsEvent = ChatStreamResponse("", references)
    val data      = Ws.Text(ChatStreamResponse.toEventString(refsEvent))
    actor.send(data)

    relevantContent
  }

  initialize()

  Logic.ingestDocuments()
}
