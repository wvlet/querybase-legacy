package wvlet.querybase.api.frontend

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime
import wvlet.querybase.api.backend.v1.ServiceCatalogApi
import wvlet.querybase.api.BuildInfo
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo

import java.time.Instant
import java.util.UUID

@RPC
trait FrontendApi {
  private val serviceStartTimeMillis = System.currentTimeMillis()

  import FrontendApi._
  def serverInfo: ServerInfo = {
    ServerInfo(upTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - serviceStartTimeMillis))
  }

  def serverNodes: Seq[ServerNode]
  def serviceCatalog: Seq[ServiceCatalogApi.Service]

  def submitQuery(request: SubmitQueryRequest): SubmitQueryResponse
  def getQueryInfo(queryId: String): Option[QueryInfo]
  def listQueries(): Seq[QueryInfo]
}

object FrontendApi {
  case class ServerInfo(
      name: String = "querybase",
      version: String = BuildInfo.version,
      oauthClientId: Option[String] = None,
      upTime: ElapsedTime
  )

  case class ServerNode(name: String, address: String, startedAt: Instant, lastHeartBeatAt: Instant) {
    def upTime: ElapsedTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - startedAt.toEpochMilli)
  }

  case class SubmitQueryRequest(query: String, serviceName: String, uuid: UUID = UUID.randomUUID())
  case class SubmitQueryResponse(queryId: String)
}
