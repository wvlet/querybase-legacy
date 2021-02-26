package wvlet.querybase.server.backend

import wvlet.airframe.Design
import wvlet.airspec.AirSpec
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

/**
  */
class BackendServerTest extends AirSpec {

  override protected def design: Design = BackendServer.testDesign

  test("launch grpc server") { client: CoordinatorClient =>
    val serviceInfo = client.v1.ServerInfoApi.serviceInfo()
    info(serviceInfo)

    Thread.sleep(100)
    val nodes = client.v1.CoordinatorApi.listNodes()
    info(nodes)
  }

}
