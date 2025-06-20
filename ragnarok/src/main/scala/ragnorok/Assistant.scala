package ragnorok

import dev.langchain4j.rag.content.Content
import dev.langchain4j.service.{SystemMessage, UserMessage, TokenStream}

trait Assistant:
  @SystemMessage(
    Array(
      "You are a helpful AI assistant for RAG. Your task is to understand the user question, and provide an answer to the best of your ability using the provided contexts.\n\nYour answers are correct, high-quality, and written by an domain expert. If you don't know the answer, say so."
    )
  )
  def chat(
    @UserMessage(Array("This is the user question that needs to be answered."))
    msg: String,
    @UserMessage(Array("Additional context about the question"))
    context: List[Content]
  ): TokenStream
