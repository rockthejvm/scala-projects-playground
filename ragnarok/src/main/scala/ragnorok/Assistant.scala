package ragnorok

import dev.langchain4j.rag.content.Content
import dev.langchain4j.service.{SystemMessage, UserMessage, TokenStream}

trait Assistant:
  @SystemMessage(
    Array(
      "You are a helpful assistant. Answer the question to the best of your ability. If you don't know the answer, say so."
    )
  )
  def chat(
    @UserMessage(Array("This is the user question that needs to be answered."))
    msg:     String,
    @UserMessage(Array("Additional context about the question"))
    context: List[Content]
  ): TokenStream
