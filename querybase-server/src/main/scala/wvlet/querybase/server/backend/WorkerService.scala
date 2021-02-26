package wvlet.querybase.server.backend

import wvlet.querybase.server.backend.BackendServer.{CoordinatorClient, WorkerConfig, WorkerServer}

import javax.annotation.PostConstruct

/**
  */
class WorkerService(nodeConfig: WorkerConfig, workerServer: WorkerServer, coordinatorClient: CoordinatorClient) {

  val self = BackendServer.selfNode(nodeConfig)

  @PostConstruct
  def init: Unit = {
    coordinatorClient.v1.CoordinatorApi.register(self)
  }

}
