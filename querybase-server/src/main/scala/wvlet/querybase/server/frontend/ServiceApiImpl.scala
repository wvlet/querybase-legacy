package wvlet.querybase.server.frontend

import wvlet.querybase.api.frontend.ServiceApi
import wvlet.querybase.api.frontend.ServiceApi.ServiceNode
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

class ServiceApiImpl(coordinatorClient: CoordinatorClient) extends ServiceApi {
  override def serviceNodes: Seq[ServiceApi.ServiceNode] = {
    val nodes = coordinatorClient.v1.CoordinatorApi.listNodes()
    nodes.map { n =>
      ServiceNode(name = n.node.name, address = n.node.address, lastHeartBeatAt = n.lastHeartbeatAt)
    }
  }
}
