package ragnorok

import org.http4s.headers.`Content-Type`
import org.http4s.{Header, Headers, MediaType}
import org.typelevel.ci.CIString

object Utils:
  def readOpenAiApiKey: String =
    Option(System.getenv("OPENAI_API_KEY")).getOrElse {
      println("*** OPENAI_API_KEY environment variable not set ***")
      System.exit(1)
      ""
    }

  def readDocsDir: String =
    Option(System.getenv("RAG_DOCS_DIR")).getOrElse {
      println("*** RAG_DOCS_DIR environment variable not set ***")
      System.exit(1)
      ""
    }

  def sseHeaders: Seq[(String, String)] =
    Seq(
      "Content-Type"      -> "text/event-stream",
      "Cache-Control"     -> "no-cache",
      "Connection"        -> "keep-alive",
      "X-Accel-Buffering" -> "no",
      "Transfer-Encoding" -> "chunked"
    )

  def eventStreamHeaders: Headers =
    Headers(
      `Content-Type`(MediaType.unsafeParse("text/event-stream")),
      Header.Raw(CIString("Cache-Control"), "no-cache"),
      Header.Raw(CIString("Connection"), "keep-alive"),
      Header.Raw(CIString("X-Accel-Buffering"), "no"),
      Header.Raw(CIString("Transfer-Encoding"), "chunked")
    )
