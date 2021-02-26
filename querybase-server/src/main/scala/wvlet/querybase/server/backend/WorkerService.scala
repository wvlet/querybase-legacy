package wvlet.querybase.server.backend

import wvlet.querybase.server.backend.BackendServer.BackendClient

import javax.annotation.PostConstruct

/**
  */
class WorkerService(nodeConfig: NodeConfig, backendClient: BackendClient) {

  val self = BackendServer.selfNode(nodeConfig)

  @PostConstruct
  def init: Unit = {
    backendClient.v1.CoordinatorApi.register(self)
  }

}
