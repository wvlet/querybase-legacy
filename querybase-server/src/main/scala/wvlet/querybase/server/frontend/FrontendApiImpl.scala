package wvlet.querybase.server.frontend

import wvlet.airframe.codec.{MessageCodec, MessageCodecFactory}
import wvlet.airframe.control.Control.withResource
import wvlet.airframe.control.IO
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, QueryInfo}
import wvlet.querybase.api.backend.v1.{CoordinatorApi, ServiceCatalogApi}
import wvlet.querybase.api.frontend.FrontendApi
import wvlet.querybase.api.frontend.FrontendApi.{
  NotebookData,
  SaveNotebookResponse,
  ServerNode,
  SubmitQueryRequest,
  SubmitQueryResponse
}
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

import java.io.{File, FileWriter}

class FrontendApiImpl(coordinatorClient: CoordinatorClient) extends FrontendApi with LogSupport {
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

  private val notebookDataCodec = MessageCodecFactory.defaultFactoryForMapOutput.of[NotebookData]
  override def saveNotebook(request: FrontendApi.SaveNotebookRequest): FrontendApi.SaveNotebookResponse = {
    info(request)

    // TODO Support multiple-users
    val sessionStorePath = new File(".querybase", "sessions")
    sessionStorePath.mkdirs()
    val sessionFile = new File(sessionStorePath, request.session.id)
    val json        = notebookDataCodec.toJson(request.data)
    withResource(new FileWriter(sessionFile)) { out =>
      info(s"Saving the session to ${sessionFile}")
      out.write(json)
    }
    SaveNotebookResponse()
  }
}
