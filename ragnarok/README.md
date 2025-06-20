# RAGnarok

RAGnarok is a demo of a Retrieval-Augmented Generation (RAG) system that uses an in-memory
embedding and database to store and retrieve information. It is designed to showcase how RAG can
be implemented in a simple and effective way.

## INFO

- The chat user interface is accessible at http://localhost:8080/static/index.html
- Backend server is a Scala-based application that uses in-memory storage for embeddings and
	database.
- User documents can be indexed by defining the environment variable `RAG_DOCS_DIR` to point to a
	directory containing files.
- Environment variable `OPENAI_API_KEU` is also required.