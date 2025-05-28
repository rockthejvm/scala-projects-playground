package com.rtjvm.chat.backend

import com.rtjvm.chat.shared.models.NewMessage
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.postgresql.ds.PGSimpleDataSource
import scalasql.*
import scalasql.core.SqlStr.SqlStringSyntax
import scalasql.core.{Config, DbClient}
import scalasql.dialects.PostgresDialect
import scalasql.dialects.PostgresDialect.*

import java.time.{Instant, LocalDateTime, ZoneOffset}

class Postgres(dataDir: String, dbName: String, dbPort: Int) {
  private val db     = startPostgres(dataDir, dbPort)
  private val client = initDb(dbName, dbPort)

  def messages: Seq[Msg[Sc]] =
    client.getAutoCommitClientConnection.runSql[Msg[Sc]](sql"select * from msg")

  def messages(sender: String): Seq[Msg[Sc]] =
    if sender.isBlank then messages
    else client.getAutoCommitClientConnection.run(Msg.select.filter(_.sender.contains(sender)))

  def saveMsg(msg: NewMessage): Unit =
    client.transaction { db =>
      val ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(msg.timestamp), ZoneOffset.UTC)
      db.run(Msg.insert.columns(_.sender := msg.sender, _.msg := msg.msg, _.parent := msg.parent, _.sentTs := ts))
      db.updateRaw("NOTIFY msg")
    }

  def saveMsg(sender: String, msg: String, timestamp: Long): Unit =
    client.transaction { db =>
      val ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
      db.run(Msg.insert.columns(_.sender := sender, _.msg := msg, _.sentTs := ts))
    }

  private def initDb(dbName: String, dbPort: Int) = {
    implicit val pgDialect: PostgresDialect = PostgresDialect

    val client = new DbClient.DataSource(datasource(), new Config {}, Seq.empty)

    client.transaction { db =>
      db.updateRaw(
        s"""
           |create table if not exists msg (
           |  id serial primary key,
           |  sender text not null,
           |  msg text not null,
           |  parent int,
           |  sent_ts timestamp not null
           |);
           |""".stripMargin
      )
    }

    client
  }

  private def startPostgres(dataDir: String, dbPort: Int) =
    EmbeddedPostgres
      .builder()
      .setDataDirectory(dataDir)
      .setCleanDataDirectory(false)
      .setPort(dbPort)
      .start()

  private def datasource(dbName: Option[String] = None) = {
    val ds = new PGSimpleDataSource()
    ds.setUser("postgres")
    dbName.foreach(ds.setDatabaseName)
    ds
  }
}
