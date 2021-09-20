package wvlet.querybase.server.backend.search

import wvlet.airframe.jdbc.DbConfig
import wvlet.airframe.ulid.ULID
import wvlet.airspec.AirSpec

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
    info(task)
  }

}
