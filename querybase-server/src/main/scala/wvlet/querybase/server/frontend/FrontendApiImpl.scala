package wvlet.querybase.server.frontend

import wvlet.airframe.sql.model.LogicalPlanPrinter
import wvlet.airframe.sql.parser.{SQLGenerator, SQLParser}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, QueryInfo}
import wvlet.querybase.api.backend.v1.{CoordinatorApi, ServiceCatalogApi}
import wvlet.querybase.api.frontend.FrontendApi
import wvlet.querybase.api.frontend.FrontendApi._
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

class FrontendApiImpl(coordinatorClient: CoordinatorClient, notebookManager: NotebookManager)
    extends FrontendApi
    with LogSupport {
  override def serverNodes: Seq[ServerNode] = {
    val nodes = coordinatorClient.v1.CoordinatorApi.listNodes()
    nodes.map { n =>
      ServerNode(
        name = n.node.name,
        address = n.node.address,
        startedAt = n.node.startedAt,
        lastHeartBeatAt = n.lastHeartbeatAt
      )
    }
  }

  override def serviceCatalog: Seq[ServiceCatalogApi.Service] = {
    coordinatorClient.v1.ServiceCatalogApi.listServices()
  }

  override def submitQuery(request: SubmitQueryRequest): SubmitQueryResponse = {
    val r =
      coordinatorClient.v1.CoordinatorApi.newQuery(NewQueryRequest(request.query, request.serviceName, request.schema))
    SubmitQueryResponse(r.queryId)
  }

  override def getQueryInfo(queryId: String): Option[QueryInfo] = {
    coordinatorClient.v1.CoordinatorApi.getQueryInfo(queryId)
  }

  override def listQueries(): Seq[CoordinatorApi.QueryInfo] = {
    coordinatorClient.v1.CoordinatorApi.listQueries()
  }

  override def saveNotebook(request: FrontendApi.SaveNotebookRequest): FrontendApi.SaveNotebookResponse = {
    notebookManager.saveNotebook(request.session, request.data)
    SaveNotebookResponse()
  }

  override def getNotebook(request: FrontendApi.GetNotebookRequest): Option[NotebookData] = {
    notebookManager.readNotebook(request.session)
  }

  override def formatQuery(query: String): String = {
    val plan = SQLParser.parse(query)
    SQLGenerator.print(plan)
  }
}
