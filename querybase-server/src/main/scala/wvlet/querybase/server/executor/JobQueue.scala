package wvlet.querybase.server.executor

import java.time.Instant

import wvlet.airframe.control.ULID
import wvlet.airframe._
import wvlet.airframe.jdbc.{ConnectionPool, ConnectionPoolFactory, DbConfig}
import wvlet.airframe.json.Json
import wvlet.querybase.store.SQLHelper

case class JobQueueConfig(dbConfig: DbConfig = DbConfig.ofSQLite(".querybase/job_queue.sqlite"))

/**
  *
  */
object JobQueue {

  type JobQueueConnectionPool = ConnectionPool

  def design(config: JobQueueConfig = JobQueueConfig()): Design = {
    newDesign
      .bind[JobQueue].toSingleton
      .bind[JobQueueConfig].toInstance(config)
      .bind[JobQueueConnectionPool].toProvider { f: ConnectionPoolFactory =>
        f.newConnectionPool(config.dbConfig)
      }
  }

  case class JobEntry(
      id: ULID = ULID.newULID,
      jobType: String,
      state: JobState = JobState.QUEUED,
      jobData: Json = "{}",
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ) {
    def withState(newState: JobState): JobEntry = {
      this.copy(state = newState, updatedAt = Instant.now())
    }
  }

}

trait JobQueue {
  import JobQueue._

  private val connectionPool = bind[JobQueueConnectionPool]
  private val jobEntryTable  = "job_entry"

  connectionPool.executeUpdate(
    SQLHelper.createTableSQLFor[JobEntry](jobEntryTable, Map("id" -> "primary key"))
  )
  connectionPool.executeUpdate(
    s"create index if not exists job_queue_id_index on ${jobEntryTable} (id)"
  )

  def add(e: JobEntry): Unit = {
    connectionPool.withConnection { implicit conn =>
      SQLHelper.insertRecord[JobEntry](jobEntryTable, e)
    }
  }

  def list: Seq[JobEntry] = {
    connectionPool.withConnection { implicit conn =>
      SQLHelper.readAs[JobEntry](jobEntryTable, s"select * from ${jobEntryTable}")
    }
  }

  def updateState(e: JobEntry): Unit = {
    connectionPool.withConnection { implicit conn =>
      SQLHelper.updateRecord[JobEntry](jobEntryTable, e, "id", Seq("state, updatedAt"))
    }
  }

}
