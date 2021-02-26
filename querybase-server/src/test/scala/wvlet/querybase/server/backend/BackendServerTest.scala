package wvlet.querybase.server.backend

import wvlet.airframe.Design
import wvlet.airspec.AirSpec
import wvlet.querybase.server.backend.BackendServer.BackendClient

/**
  */
class BackendServerTest extends AirSpec {

  override def design: Design = BackendServer.testDesign

  test("launch grpc server") { client: BackendClient =>
    val serviceInfo = client.v1.ServerInfoApi.serviceInfo()
    info(serviceInfo)
    val nodes = client.v1.CoordinatorApi.listNodes()
    info(nodes)
  }

}
