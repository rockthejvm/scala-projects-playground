package ragnorok

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocumentsRecursively
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.chat.{ChatModel, StreamingChatModel}
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.openai.{OpenAiChatModel, OpenAiEmbeddingModel, OpenAiStreamingChatModel}
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import io.circe.generic.auto.deriveEncoder
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

import java.nio.file.FileSystems
import scala.jdk.CollectionConverters.*

object ChatService:
  private val logger         = LoggerFactory[IO].getLogger
  private val openaiApiKey   = readOpenAiApiKey
  private val DocsDir        = readDocsDir
  private val pathMatcher    = FileSystems.getDefault.getPathMatcher("glob:**.scala")
  private val documents      = loadDocumentsRecursively(DocsDir, pathMatcher, new TextDocumentParser())
  private val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()

  def routes(implicit logger: Logger[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] { case req @ POST -> Root / "chat" =>
      for {
        _               <- logger.info("Received chat request")
        chatReq         <- req.as[ChatRequest]
        responsePromise <- IO.deferred[String]
        _ <- IO.blocking {
          try {
            val tokenStream = ChatService.assistant.chat(chatReq.question)

            tokenStream
              .onPartialResponse(partialResponse => println(partialResponse))
              .onRetrieved(contents => System.out.println(contents))
              .onToolExecuted(toolExecution => System.out.println(toolExecution))
              .onCompleteResponse { response =>
                System.out.println(response)
                responsePromise.complete(response.aiMessage.text).unsafeRunSync()
              }
              .onError(error => error.printStackTrace())
              .start();
          } catch {
            case e: Throwable =>
              responsePromise.complete(e.getMessage).unsafeRunSync()
          }
        }
        answer <- responsePromise.get
        resp   <- Ok(ChatResponse(answer).asJson)
      } yield resp
    }

  val embeddingModel: OpenAiEmbeddingModel =
    OpenAiEmbeddingModel
      .builder()
      .apiKey(openaiApiKey)
      .modelName("text-embedding-ada-002")
      .build()

  ingestDocs(documents, embeddingStore, embeddingModel).start.unsafeRunSync()

  val chatModel: StreamingChatModel =
    OpenAiStreamingChatModel
      .builder()
      .apiKey(openaiApiKey)
      .modelName("gpt-4o-mini")
      .build()

  val assistant: Assistant =
    AiServices
      .builder(classOf[Assistant])
      .streamingChatModel(chatModel)
      .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
      .contentRetriever(
        EmbeddingStoreContentRetriever
          .builder()
          .embeddingStore(embeddingStore)
          .embeddingModel(embeddingModel)
          .build()
      )
      .build()

  private def ingestDocs(
    documents:      java.util.List[Document],
    embeddingStore: EmbeddingStore[TextSegment],
    embeddingModel: EmbeddingModel
  ): IO[Unit] = {
    val splitter = DocumentSplitters.recursive(300, 0)

    val segments =
      for
        document <- documents.asScala
        segment  <- splitter.split(document).asScala
      yield TextSegment.from(segment.text(), segment.metadata())

    // Process segments in parallel, off the main thread
    segments.toList.parTraverse_ { textSegment =>
      IO.blocking {
        val response  = embeddingModel.embed(textSegment.text())
        val embedding = response.content()
        embeddingStore.add(embedding, textSegment)
      }
    }
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
