package wvlet.querybase.store

import wvlet.log.LogSupport

/** */
class SQLiteQueryStorage extends QueryStorage with LogSupport {
  override def add(q: QueryList): Unit = {
    info(q)
  }
}
