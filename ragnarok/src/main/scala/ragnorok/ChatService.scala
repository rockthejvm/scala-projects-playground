package ragnorok

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocumentsRecursively
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.{OpenAiEmbeddingModel, OpenAiStreamingChatModel}
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import fs2.Stream
import fs2.concurrent.Channel
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.typelevel.ci.CIString
import org.typelevel.log4cats.{Logger, LoggerFactory}

import java.nio.file.FileSystems
import scala.jdk.CollectionConverters.*

object ChatService:
  private val logger         = LoggerFactory[IO].getLogger
  private val openaiApiKey   = readOpenAiApiKey
  private val DocsDir        = readDocsDir
  private val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()

  private val pathMatcher = FileSystems.getDefault.getPathMatcher(
    "glob:**/*.{scala,java,py,txt,md,mdx,json,jsonl,html,csv,tsv,pdf}"
  )

  private val documents = {
    logger.info(s"Loading documents from directory: $DocsDir").unsafeRunAndForget()
    loadDocumentsRecursively(DocsDir, pathMatcher, new TextDocumentParser())
  }

  val embeddingModel: OpenAiEmbeddingModel =
    OpenAiEmbeddingModel
      .builder()
      .apiKey(openaiApiKey)
      .modelName("text-embedding-ada-002")
      .build()

  val chatModel: StreamingChatModel =
    OpenAiStreamingChatModel
      .builder()
      .apiKey(openaiApiKey)
      .modelName("gpt-4o-mini")
      .build()

  private val contentRetriever =
    EmbeddingStoreContentRetriever
      .builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .build()

  val assistant: Assistant =
    AiServices
      .builder(classOf[Assistant])
      .streamingChatModel(chatModel)
      .chatMemory(MessageWindowChatMemory.withMaxMessages(15))
      .contentRetriever(contentRetriever)
      .build()

  ingestDocuments()

  def routes(implicit logger: Logger[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] { case req @ POST -> Root / "chat" =>
      logger.info("Received chat request") *> {
        (
          for {
            chatReq  <- req.as[ChatRequest]
            channel  <- Channel.unbounded[IO, String]
            contents <- retrieveContext(chatReq.question, channel)

            _ <- IO {
              ChatService.assistant
                .chat(chatReq.question, contents)
                .onPartialResponse { token =>
                  val json  = Json.obj("content" -> Json.fromString(token))
                  val event = s"data: ${json.noSpaces}\n\n"
                  channel.send(event).unsafeRunSync()
                }
                .onCompleteResponse { _ =>
                  channel.send("data: [DONE]\n\n").unsafeRunSync()
                  channel.close.attempt.void.unsafeRunSync()
                }
                .onError { error =>
                  channel.send(s"event: error\ndata: ${error.getMessage}\n\n").unsafeRunAndForget()
                  channel.close.attempt.void.unsafeRunAndForget()
                }
                .start()
            }.start
          } yield {
            Response[IO](
              status  = Status.Ok,
              body    = channel.stream.flatMap(str => Stream.emits(str.getBytes)),
              headers = eventStreamHeaders
            )
          }
        ).handleErrorWith { error =>
          logger.error(s"Error processing chat request: ${error.getMessage}") *>
            InternalServerError(error.getMessage)
        }
      }
    }

  private def retrieveContext(question: String, channel: Channel[IO, String]): IO[List[Content]] = IO {
    val query           = Query.from(question)
    val relevantContent = contentRetriever.retrieve(query).asScala.toList

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
    channel.send(ChatStreamResponse.toEventString(refsEvent)).unsafeRunSync()
    relevantContent
  }.handleErrorWith { error =>
    logger.error(error)("Error during retrieval") *>
      channel.send(s"event: error\ndata: ${error.getMessage}\n\n") *>
      channel.close.attempt.void

    IO.pure(List.empty[Content])
  }

  private def ingestDocuments(): Unit = {
    logger.info("Ingesting documents for RAG...This may take a while!").unsafeRunAndForget()

    val textSegments =
      documents.asScala
        .flatMap { doc =>
          logger.info(s"  Processing document: ${doc.metadata().getString("file_name")}").unsafeRunAndForget()
          val splitter = DocumentSplitters.recursive(1000, 200)
          splitter.split(doc).asScala.map(segment => TextSegment.from(segment.text(), doc.metadata()))
        }
        .toList
        .asJava

    val embeddings = embeddingModel.embedAll(textSegments).content()
    embeddingStore.addAll(embeddings, textSegments)
  }

  private def readOpenAiApiKey: String =
    Option(System.getenv("OPENAI_API_KEY")).getOrElse {
      println("*** OPENAI_API_KEY environment variable not set ***")
      System.exit(1)
      ""
    }

  private def readDocsDir: String =
    Option(System.getenv("RAG_DOCS_DIR")).getOrElse {
      println("*** RAG_DOCS_DIR environment variable not set ***")
      System.exit(1)
      ""
    }

  private def eventStreamHeaders =
    Headers(
      `Content-Type`(MediaType.unsafeParse("text/event-stream")),
      Header.Raw(CIString("Cache-Control"), "no-cache"),
      Header.Raw(CIString("Connection"), "keep-alive"),
      Header.Raw(CIString("X-Accel-Buffering"), "no"),
      Header.Raw(CIString("Transfer-Encoding"), "chunked")
    )
