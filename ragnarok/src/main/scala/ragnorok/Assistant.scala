package ragnorok

import dev.langchain4j.service.TokenStream

trait Assistant:
  def chat(msg: String): TokenStream
