package wvlet.querybase.store

import wvlet.querybase.api.backend.v1.query.QueryLogApi.QueryLog

/** */
trait QueryStorage {
  def add(q: QueryList)
}

case class QueryList(queries: Seq[QueryLog])
