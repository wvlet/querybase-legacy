package wvlet.querybase.server.backend.query

import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, QueryInfo}
import wvlet.querybase.api.backend.v1.ServiceCatalogApi
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.api.ServiceCatalog

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

case class QueryId(id: String) {
  override def toString: String = id
}

/**
  */
class QueryManager(catalog: ServiceCatalog) {

  private val queryList        = new ConcurrentHashMap[String, QueryInfo]().asScala
  private val queryIdGenerator = new QueryIdGenerator()

  def newQuery(request: NewQueryRequest): QueryInfo = {

    val queryId = queryIdGenerator.newQueryId

    // Find the target service from the catalog
    val serviceName = request.serviceName
    val qi: QueryInfo = catalog.services.find(_.name == serviceName) match {
      case Some(svc) =>
        QueryInfo(
          queryId = queryId.toString,
          serviceName = svc.name,
          serviceType = svc.serviceType,
          queryStatus = QueryStatus.QUEUED,
          query = request.query
        )
      case None =>
        QueryInfo(
          queryId = queryId.toString,
          serviceName = request.serviceName,
          serviceType = "N/A",
          queryStatus = QueryStatus.FAILED,
          query = request.query
        )
    }

    queryList.put(qi.queryId, qi)
    // TODO Process query
    qi
  }

  def listQueries: Seq[QueryInfo] = {
    queryList.values.toSeq
  }
}
