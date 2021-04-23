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

  // Notebook APIs
  def saveNotebook(request: SaveNotebookRequest): SaveNotebookResponse
  def getNotebook(request: GetNotebookRequest): Option[NotebookData]

  def formatQuery(query: String): String
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

  case class SubmitQueryRequest(
      query: String,
      serviceName: String,
      schema: Option[String] = None,
      uuid: UUID = UUID.randomUUID()
  )
  case class SubmitQueryResponse(queryId: String)

  case class SaveNotebookRequest(
      session: NotebookSession,
      data: NotebookData
  )
  case class SaveNotebookResponse(
  )

  case class NotebookSession(id: String)
  case class NotebookData(cells: Seq[NotebookCellData])
  case class NotebookCellData(
      text: String,
      queryInfo: Option[QueryInfo]
  )

  case class GetNotebookRequest(
      session: NotebookSession
  )
}
