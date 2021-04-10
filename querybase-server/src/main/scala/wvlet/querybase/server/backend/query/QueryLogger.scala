package wvlet.querybase.server.backend.query

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo

class QueryLogger extends LogSupport {
  def startLog(queryInfo: QueryInfo): Unit = {
    info(s"query started: ${queryInfo}")
  }
  def completionLog(queryInfo: QueryInfo): Unit = {
    info(s"query completed: ${queryInfo}")
  }
}
