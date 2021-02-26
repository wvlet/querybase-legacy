package wvlet.querybase.server.frontend

import wvlet.airframe.Design
import wvlet.airframe.http.Http
import wvlet.airspec.AirSpec
import wvlet.querybase.server.frontend.FrontendServer.FrontendClient

/**
  */
class FrontendServerTest extends AirSpec {

  override protected def design: Design = FrontendServer.testDesign

  test("provide index.html") { client: FrontendClient =>
    val resp = client.getClient.send(Http.GET("/ui/index.html"))
    resp.statusCode shouldBe 200
  }

  test("rpc service info") { client: FrontendClient =>
    val serviceInfo = client.ServiceApi.serviceInfo()
    serviceInfo.name shouldBe "querybase"
  }
}
