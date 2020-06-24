package wvlet.querybase.server

import com.twitter.finagle.http.Request
import wvlet.airframe.Design
import wvlet.airframe.http.finagle.FinagleSyncClient
import wvlet.airspec.AirSpec

/**
  */
class QuerybaseServerTest extends AirSpec {

  override protected def design: Design = QuerybaseServer.testDesign

  test("provide index.html") { client: FinagleSyncClient =>
    val resp = client.send(Request("/ui/index.html"))
    resp.statusCode shouldBe 200
  }
}
