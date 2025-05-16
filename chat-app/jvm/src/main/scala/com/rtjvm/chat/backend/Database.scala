package com.rtjvm.chat.backend

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import com.rtjvm.chat.shared.models.Message
import io.getquill.*

import java.io.File

final class Database(dbName: String, dataDir: File) {
  private val db  = startMySQL(dataDir)
  private val ctx = initDb(dbName)

  import ctx.*

  def messages: List[Message] = ctx.run(query[Message]) // .map(m => (m.name, m.msg)))
  
  def saveMsg(msg: Message): Unit = 
    ctx.run(query[Message].insertValue(lift(msg)))

  private def initDb(dbName: String): MysqlJdbcContext[LowerCase] =
    db.createDB(dbName)
    val config = new com.zaxxer.hikari.HikariConfig()
    config.setDriverClassName("com.mysql.cj.jdbc.Driver")
    config.setJdbcUrl(s"jdbc:mysql://localhost:${db.getConfiguration.getPort}/$dbName")
    config.setUsername("root")
    config.setPassword("")
    val ctx = new MysqlJdbcContext(LowerCase, new com.zaxxer.hikari.HikariDataSource(config))
    initSchema(ctx)
    ctx

  private def startMySQL(dataDir: File): DB =
    val config = DBConfigurationBuilder.newBuilder()
    config.setPort(3307)
    config.setDataDir(dataDir)
    val db = DB.newEmbeddedDB(config.build())
    db.start()
    db

  private def initSchema(ctx: MysqlJdbcContext[LowerCase]): Unit =
    val createTable = quote {
      sql"create table if not exists message (name text, msg text)".as[Insert[Message]]
    }
    ctx.run(createTable)
}

object Database:

  def startMySQL(dataDir: File): DB =
    val config = DBConfigurationBuilder.newBuilder()
    config.setPort(3307)
    config.setDataDir(dataDir)
    val db = DB.newEmbeddedDB(config.build())
    db.start()
    db

  def initDb(db: DB, dbName: String): MysqlJdbcContext[LowerCase] =
    db.createDB(dbName)
    val config = new com.zaxxer.hikari.HikariConfig()
    config.setDriverClassName("com.mysql.cj.jdbc.Driver")
    config.setJdbcUrl(s"jdbc:mysql://localhost:${db.getConfiguration.getPort}/$dbName")
    config.setUsername("root")
    config.setPassword("")
    val ctx = new MysqlJdbcContext(LowerCase, new com.zaxxer.hikari.HikariDataSource(config))
    initSchema(ctx)
    ctx

  private def initSchema(ctx: MysqlJdbcContext[LowerCase]): Unit =
    val createTable = quote {
      sql"create table if not exists message (name text, msg text)".as[Insert[Message]]
    }
    ctx.run(createTable)

end Database
