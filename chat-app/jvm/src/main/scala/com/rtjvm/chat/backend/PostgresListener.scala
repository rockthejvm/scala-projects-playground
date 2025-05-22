package com.rtjvm.chat.backend

import com.impossibl.postgres.api.jdbc.{PGConnection, PGNotificationListener}
import com.impossibl.postgres.jdbc.PGDataSource

import java.util.concurrent.atomic.AtomicBoolean
import scala.compiletime.uninitialized
import scala.util.Using

class PostgresListener {

  private var connection: PGConnection = uninitialized
  private val isListening = new AtomicBoolean(false)

  def startListening(): Unit =
    if !isListening.compareAndSet(false, true) then println("Already listening, skipping new listener startup.")
    else {
      new Thread(() => {
        try {
          setupListener()

          // To keep the listener and the connection alive!
          while (isListening.get()) {
            Thread.sleep(5000)
          }
        } catch {
          case e: Exception =>
            println(s"Error starting listener: ${e.getMessage}")
        }
      }).start()
    }

  def stopListening(): Unit = {
    if (isListening.compareAndSet(true, false)) {
      println("*** LISTEN *** Stopping listener...")

      if (connection != null) {
        try {
          connection.close() // Close the connection when done
          println("*** LISTEN *** Connection closed manually")
        } catch {
          case e: Exception =>
            println(s"Error closing connection: ${e.getMessage}")
        }
      }
    }
  }

  private def setupListener(): Unit =
    val raw = new PGDataSource().getConnection("postgres", "")
    connection = raw.unwrap(classOf[PGConnection])

    connection.addNotificationListener(new PGNotificationListener {
      override def notification(processId: Int, channelName: String, payload: String): Unit = {
        println(s"*** LISTEN Notification ***: $processId, $channelName, $payload")
      }

      override def closed(): Unit = {
        println("*** LISTEN *** Connection closed")
        // Reconnect or handle closed connection here if needed
      }
    })

    Using(connection.createStatement()) { stmt =>
      stmt.executeUpdate("LISTEN msg")
    }

    println("Listening for notifications on channel 'msg'...")
}
