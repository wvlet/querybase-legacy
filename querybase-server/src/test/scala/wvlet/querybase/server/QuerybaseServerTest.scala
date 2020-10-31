package wvlet.querybase.server

import wvlet.airframe.Design
import wvlet.airspec.AirSpec
import wvlet.querybase.server.QuerybaseServer.QuerybaseSyncClient
import wvlet.airframe.http.Http

/**
  */
class QuerybaseServerTest extends AirSpec {

  override protected def design: Design = QuerybaseServer.testDesign

  test("provide index.html") { client: QuerybaseSyncClient =>
    val resp = client.getClient.send(Http.GET("/ui/index.html"))
    resp.statusCode shouldBe 200
  }

  test("rpc service info") { client: QuerybaseSyncClient =>
    val serviceInfo = client.ServiceApi.serviceInfo()
    serviceInfo.name shouldBe "querybase"
  }
}
