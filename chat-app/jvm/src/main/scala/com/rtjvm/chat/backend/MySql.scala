package com.rtjvm.chat.backend

import ch.vorburger.mariadb4j.{DB, DBConfigurationBuilder}
import com.mysql.cj.jdbc.MysqlDataSource
import scalasql.*
import scalasql.MySqlDialect.*
import scalasql.core.{Config, DbClient}

import java.io.File
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

class MySql(dbName: String, dataDir: File) {

  private final val DbPort = 3307

  private val db     = startMySQL(dataDir, DbPort)
  private val client = initializeDb(dbName, DbPort)

  def messages: Seq[Msg[Sc]] = client.transaction { db =>
    db.run(Msg.select)
  }

  def saveMsg(sender: String, msg: String, timestamp: Long): Unit = client.transaction { db =>
    val ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    db.run(Msg.insert.columns(_.sender := sender, _.msg := msg, _.sentTs := ts))
  }

  private def initializeDb(dbName: String, dbPort: Int) = {
    createDatabase(dbName, dbPort)

    createClient(dbName, dbPort) { client =>
      client.transaction { db =>
        db.updateRaw(
          """
            |create table if not exists msg (
            |sender text not null, 
            |msg text not null, 
            |sent_ts datetime default current_timestamp)""".stripMargin
        )
      }
    }
  }

  private def createDatabase(dbName: String, dbPort: Int): Unit = {
    val ds = new MysqlDataSource
    ds.setURL(s"jdbc:mysql://localhost:$dbPort?allowMultiQueries=true")
    ds.setUser("root")
    ds.setPassword("")

    val client = new DbClient.DataSource(ds, new Config {})
    client.transaction(_.updateRaw(s"create database if not exists $dbName"))
  }

  private def createClient(dbName: String, dbPort: Int)(init: DbClient.DataSource => Unit) = {
    val ds = new com.mysql.cj.jdbc.MysqlDataSource
    ds.setURL(s"jdbc:mysql://localhost:$dbPort/$dbName?allowMultiQueries=true")
    ds.setUser("root")
    ds.setPassword("")

    val client = new DbClient.DataSource(ds, new Config {})
    init(client)
    client
  }

  private def startMySQL(dataDir: File, dbPort: Int): DB =
    val config = DBConfigurationBuilder.newBuilder()
    config.setPort(dbPort)
    config.setDataDir(dataDir)
    val db = DB.newEmbeddedDB(config.build())
    db.start()
    db
}
