package com.rtjvm.chat.backend

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.postgresql.ds.PGSimpleDataSource
import scalasql.*
import scalasql.core.{Config, DbClient}
import scalasql.dialects.PostgresDialect.*

class Database(dataDir: String, dbName: String, dbPort: Int) {
  private val db     = startPostgres(dataDir, dbPort)
  private val client = initDb(dbName, dbPort)

  def messages: Seq[Msg[Sc]] = client.transaction { db =>
    db.run(Msg.select)
  }

  private def initDb(dbName: String, dbPort: Int) = {
    implicit val pgDialect: PostgresDialect = PostgresDialect

    val pgDataSource = new PGSimpleDataSource()
    pgDataSource.setUser("postgres")

    val client = new DbClient.DataSource(pgDataSource, new Config {})
    client.transaction { db =>
      db.updateRaw(s"create database if not exists $dbName")

      db.updateRaw(
        s"""
           |create table if not exists msg (
           |  id serial primary key,
           |  sender text not null,
           |  msg text not null,
           |  sentTs timestamp not null
           |);
           |""".stripMargin
      )
    }

    client
  }

  private def startPostgres(dataDir: String, dbPort: Int) =
    EmbeddedPostgres
      .builder()
      // .setDataDirectory(dataDir)
      .setCleanDataDirectory(false)
      .setPort(5432)
      .start()
}
