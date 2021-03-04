package wvlet.querybase.server.frontend

import wvlet.querybase.api.backend.v1.ServiceCatalogApi
import wvlet.querybase.api.frontend.ServiceApi
import wvlet.querybase.api.frontend.ServiceApi.ServiceNode
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

class ServiceApiImpl(coordinatorClient: CoordinatorClient) extends ServiceApi {
  override def serviceNodes: Seq[ServiceApi.ServiceNode] = {
    val nodes = coordinatorClient.v1.CoordinatorApi.listNodes()
    nodes.map { n =>
      ServiceNode(
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
}
