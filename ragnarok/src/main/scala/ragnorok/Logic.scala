package ragnorok

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocumentsRecursively
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.{OpenAiEmbeddingModel, OpenAiStreamingChatModel}
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.typelevel.log4cats.LoggerFactory

import java.nio.file.FileSystems
import java.util
import scala.jdk.CollectionConverters.*

object Logic:
  private val logger         = LoggerFactory[IO].getLogger
  private val openaiApiKey   = Utils.readOpenAiApiKey
  private val DocsDir        = Utils.readDocsDir
  private val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()

  private val pathMatcher = FileSystems.getDefault.getPathMatcher(
    "glob:**/*.{scala,java,py,txt,md,mdx,json,jsonl,html,csv,tsv,pdf}"
  )

  val documents: util.List[Document] = {
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

  val contentRetriever: EmbeddingStoreContentRetriever =
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

  def ingestDocuments(): Unit = {
    logger.info("Ingesting documents for RAG...This may take a while!").unsafeRunAndForget()

    val textSegments =
      documents.asScala
        .flatMap { doc =>
          val path = java.nio.file.Paths.get(doc.metadata().getString("absolute_directory_path"), doc.metadata().getString("file_name"))
          logger.info(s"  Processing document: $path").unsafeRunAndForget()
          val splitter = DocumentSplitters.recursive(1000, 200)
          splitter.split(doc).asScala.map(segment => TextSegment.from(segment.text(), doc.metadata()))
        }
        .toList
        .asJava

    val embeddings = embeddingModel.embedAll(textSegments).content()
    embeddingStore.addAll(embeddings, textSegments)
  }
