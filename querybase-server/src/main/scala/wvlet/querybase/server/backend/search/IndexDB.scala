package wvlet.querybase.server.backend.search

import wvlet.airframe._
import wvlet.airframe.jdbc.{ConnectionPool, ConnectionPoolFactory, DbConfig}
import wvlet.airframe.metrics.ElapsedTime
import wvlet.log.LogSupport

import java.time.Instant

case class IndexDBConfig(
    dbConfig: DbConfig = DbConfig.ofSQLite(".querybase/indexing/indexing.db"),
    tableName: String = "indexing_task"
)

import IndexDB._

/** Stores indexing task state
  */
class IndexDB(config: IndexDBConfig, connectionPool: IndexDBConnectionPool) extends LogSupport {
  init

  private def init: Unit = {
    info(s"Initializing IndexDB...")
    val sql = SQLHelper.createTableSQLFor[IndexingTask](config.tableName, paramOption = Map("id" -> "primary key"))
    connectionPool.executeUpdate(sql)
  }

  def newTask(id: String): IndexingTask = {
    val now  = Instant.now()
    val task = IndexingTask(id = id, created = now, state = TaskState.QUEUED, lastUpdated = now)
    connectionPool.withConnection { implicit conn =>
      SQLHelper.insertRecord(config.tableName, task)
    }
    task
  }

  def getTask(id: String): Option[IndexingTask] = {
    connectionPool.withConnection { implicit conn =>
      val tasks = SQLHelper.readAs[IndexingTask](
        s"""select * from "${config.tableName}" where id = '${id}'"""
      )
      tasks.headOption
    }
  }

  def getOrCreate(id: String): IndexingTask = {
    getTask(id).getOrElse(newTask(id))
  }

}

object IndexDB {

  type IndexDBConnectionPool = ConnectionPool

  def design(indexDBConfig: IndexDBConfig = IndexDBConfig()): Design = {
    newDesign
      .bind[IndexDBConfig].toInstance(indexDBConfig)
      .bind[IndexDBConnectionPool].toProvider { (config: IndexDBConfig, factory: ConnectionPoolFactory) =>
        factory.newConnectionPool(config.dbConfig)
      }
  }

  case class IndexingTask(
      id: String,
      created: Instant,
      state: TaskState,
      lastUpdated: Instant
  )

  sealed trait TaskState
  object TaskState {
    case object QUEUED   extends TaskState
    case object RUNNING  extends TaskState
    case object FINISHED extends TaskState
    case object FAILED   extends TaskState

    def all = Seq(QUEUED, RUNNING, FINISHED, FAILED)

    def unapply(s: String): Option[TaskState] = {
      all.find(_.toString == s)
    }
  }

}
