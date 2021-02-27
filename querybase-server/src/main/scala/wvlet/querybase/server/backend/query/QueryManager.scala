package wvlet.querybase.server.backend.query

import wvlet.querybase.api.backend.v1.CoordinatorApi.NewQueryRequest

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

case class QueryId(id: String) {
  override def toString: String = id
}
case class QueryInfo(queryId: QueryId)

/**
  */
class QueryManager {

  private val queryList        = new ConcurrentHashMap[QueryId, QueryInfo]().asScala
  private val queryIdGenerator = new QueryIdGenerator()

  def newQuery(request: NewQueryRequest): QueryInfo = {
    val qi = QueryInfo(queryIdGenerator.newQueryId)
    queryList.put(qi.queryId, qi)
    // TODO Process query
    qi
  }
}
