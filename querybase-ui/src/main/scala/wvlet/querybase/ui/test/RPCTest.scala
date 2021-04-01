package wvlet.querybase.ui.test

import wvlet.airframe.rx.{Rx, RxOption}
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.ui.RPCService

import scala.concurrent.Future

case class RxTestCase(suite: String, testName: String, body: () => Rx[_])

trait RxTest extends LogSupport {
  def name: String

  private[ui] var tests: List[RxTestCase] = List.empty[RxTestCase]

  implicit protected val queue = scalajs.concurrent.JSExecutionContext.queue

  def test(testName: String)(result: => Future[_]): Unit = {
    tests = RxTestCase(name, testName, { () => Rx.fromFuture(result) }) :: tests
  }

  def assert(cond: Boolean): Unit = {
    if (!cond) {
      throw new AssertionError(s"test failed")
    }
  }

}

class RPCTest(rpcClient: ServiceJSClient) extends RxTest {
  def name: String = "RPCTest"

  test("Read ServerInfo") {
    rpcClient.FrontendApi.serverInfo().map { i =>
      assert(i.name == "querybase")
    }
  }

  test("List server nodes") {
    rpcClient.FrontendApi.serverNodes().map { lst =>
      assert(lst.size >= 2)
    }
  }
}
