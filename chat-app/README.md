# Chat App

## Stack

- Postgres
- ScalaSql
- Scalatags
- Scala.js

## Running the application

1. Run the client with `sbt "~appJS/fastOptJS"` to keep the client files up to date with the changes you make to `js (appJS)` project.
2. You can run the server from the IDE by loading the entire project and running the `Server` class.
3. Or you can run the server from the command line from _within the `chat-app` folder_: `sbt runMain com.rtjvm.chat.backend.Server`.
4. Chat application should be accessible at http://localhost:8080/static/index.html

## Project Info

- The project is implemented as per the chapters in the book except it is in a finished state, and does not instantly show the progression in the book. For instance, the current client does not do form submit as described in the initial pages of the chapter in the book. But uses REST APIs instead.
- All outgoing responses use `Message` object to represent a message.
- API responses (currently only `/chat`) use `ChatResponse` object to represent the response, which uses `Message` object to represent a message.
- All websocket messages use `Seq[Message]` to push messages to the client.
- Followed the book even though inefficient to push all messages to the client at once; even when sending a single chat message.

## Differences:

- The project does not POST to the root path `/` to send a chat message instead POSTs to the `/chat` endpoint with a JSON body.

## Exercises

**Add HTML input to let the use filter chat by username.**

See endpoint `@cask.getJson("/messages/:searchTerm")` in `Server.scala`.

**Synchronize access to open connections using `synchronized`**

Not using `synchronized` but using a `ConcurrentHashMap`.

**The online examples so far provide a simple test suite, that uses `String.contains` .... Use the Jsoup library we saw Chapter 11: Scraping Websites to make ... tag**

Not Implemented! As we discussed, we are not doing any tests.

**Keep track each message's send time and date in the database, and display it in the user interface**

See `Postgres#initDb`

**Add the ability to reply directly to existing chat messages, ... nested arbitrarily deeply to form a tree-shaped "threaded" discussion**

- Look for `parent` on the server side
- For client side, see `Main.scala` (`Main#messageList`)

**One limitation of the current push-update mechanism is that it can only updates to
browsers connected to the same webserver. Make use of Postgres's LISTEN/NOTIFY feature ... register callbacks on these events**

See `PostgresListener`
