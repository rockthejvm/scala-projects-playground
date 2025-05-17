package com.rtjvm.chat.backend

import com.mysql.cj.jdbc.MysqlDataSource
import scalasql.core.{Config, DbClient}
import scalasql.MySqlDialect.*
import scalasql.Sc

class MySql(dbName: String) {

  private val client = createClient(dbName)

  def messages: Seq[Msg[Sc]] = client.transaction { db =>
    db.run(Msg.select)
  }

  private def initDb(dbName: String) =
    client.transaction { db =>
      db.updateRaw("create table if not exists messages (name text, msg text)")
    }

  private def createClient(dbName: String) =
    new DbClient.DataSource(
      datasource(dbName, "root", ""),
      config = new Config {}
    )

  private def datasource(username: String, password: String, dbName: String): MysqlDataSource =
    val ds = new com.mysql.cj.jdbc.MysqlDataSource
    ds.setURL(s"jdbc:mysql://localhost:3307?allowMultiQueries=true")
    ds.setDatabaseName(dbName)
    ds.setUser(username)
    ds.setPassword(password)
    ds

  initDb(dbName)
}
