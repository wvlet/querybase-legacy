package wvlet.querybase.server.backend.query

import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, QueryInfo}
import wvlet.querybase.api.backend.v1.query.QueryStatus

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

case class QueryId(id: String) {
  override def toString: String = id
}

/**
  */
class QueryManager {

  private val queryList        = new ConcurrentHashMap[String, QueryInfo]().asScala
  private val queryIdGenerator = new QueryIdGenerator()

  def newQuery(request: NewQueryRequest): QueryInfo = {
    val qi = QueryInfo(queryId = queryIdGenerator.newQueryId.id, queryStatus = QueryStatus.QUEUED, request.query)
    queryList.put(qi.queryId, qi)
    // TODO Process query
    qi
  }

  def listQueries: Seq[QueryInfo] = {
    queryList.values.toSeq
  }
}
