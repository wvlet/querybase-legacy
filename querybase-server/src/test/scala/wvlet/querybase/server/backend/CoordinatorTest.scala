package wvlet.querybase.server.backend

import wvlet.airframe.Design
import wvlet.airspec.AirSpec
import wvlet.querybase.api.backend.v1.CoordinatorApi.NewQueryRequest
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

/**
  */
class CoordinatorTest extends AirSpec {

  override protected def design: Design = BackendServer.testDesign

  test("Launch coordinator") { client: CoordinatorClient =>
    val serviceInfo = client.v1.ServerInfoApi.serverInfo()
    info(serviceInfo)

    Thread.sleep(100)
    val nodes = client.v1.CoordinatorApi.listNodes()
    info(nodes)

    val qi1 = client.v1.CoordinatorApi.newQuery(NewQueryRequest("select 1", "s1"))
    info(qi1)
    val qi2 = client.v1.CoordinatorApi.newQuery(NewQueryRequest("select 100", "s2"))
    info(qi2)

    val queries = client.v1.CoordinatorApi.listQueries()
    info(queries)
  }

}
