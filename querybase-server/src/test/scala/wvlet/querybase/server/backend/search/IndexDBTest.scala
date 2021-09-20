package wvlet.querybase.server.backend.search

import wvlet.airframe.jdbc.DbConfig
import wvlet.airframe.ulid.ULID
import wvlet.airspec.AirSpec
import wvlet.querybase.server.backend.search.IndexDB.TaskState

import java.io.File

/** */
class IndexDBTest extends AirSpec {

  private val dbFile            = s"target/.querybase/indexing-${ULID.newULIDString}.db"
  protected override def design = IndexDB.design(IndexDBConfig(DbConfig.ofSQLite(dbFile)))

  override protected def afterAll: Unit = {
    val file = new File(dbFile)
    if (file.exists()) {
      file.delete()
    }
  }

  test("create IndexDB") { (indexDB: IndexDB) =>
    val taskId = ULID.newULIDString
    val task   = indexDB.getOrCreate(taskId)
    debug(task)
    indexDB.updateTaskState(taskId, TaskState.RUNNING, metadata = Map("query_id" -> "query012345"))
    val updated = indexDB.getTask(taskId).get
    debug(updated)

    task.id shouldBe updated.id
    task.created shouldBe updated.created
    task.created.compareTo(updated.lastUpdated) <= 0 shouldBe true
    updated.state shouldBe TaskState.RUNNING
    updated.metadata shouldBe Map("query_id" -> "query012345")
  }

}
