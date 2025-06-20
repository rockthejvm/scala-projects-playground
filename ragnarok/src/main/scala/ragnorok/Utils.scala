package ragnorok

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
