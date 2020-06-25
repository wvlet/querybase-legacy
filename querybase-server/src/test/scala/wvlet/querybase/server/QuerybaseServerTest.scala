package wvlet.querybase.server

import com.twitter.finagle.http.Request
import wvlet.airframe.Design
import wvlet.airspec.AirSpec
import wvlet.querybase.server.QuerybaseServer.QuerybaseSyncClient

/**
  */
class QuerybaseServerTest extends AirSpec {

  override protected def design: Design = QuerybaseServer.testDesign

  test("provide index.html") { client: QuerybaseSyncClient =>
    val resp = client.getClient.send(Request("/ui/index.html"))
    resp.statusCode shouldBe 200
  }

  test("rpc service info") { client: QuerybaseSyncClient =>
    val serviceInfo = client.serviceApi.serviceInfo()
    serviceInfo.name shouldBe "querybase"
  }
}
