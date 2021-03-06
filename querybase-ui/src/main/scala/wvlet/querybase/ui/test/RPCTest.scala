package wvlet.querybase.ui.test

import wvlet.airframe.rx.{Rx, RxOption}
import wvlet.querybase.ui.RPCService

case class RxTestCase(suite: String, testName: String, body: () => Rx[_])

trait RxTest {
  def name: String

  private[ui] var tests: List[RxTestCase] = List.empty[RxTestCase]

  def test(testName: String)(result: => Rx[_]): Unit = {
    tests = RxTestCase(name, testName, { () => result }) :: tests
  }

  def assert(cond: Boolean): Unit = {
    if (!cond) {
      throw new AssertionError(s"test failed")
    }
  }

}

class RPCTest(rpcService: RPCService) extends RxTest {
  def name: String = "RPCTest"

  test("Read ServerInfo") {
    rpcService.rpcRx(_.FrontendApi.serverInfo()).map { i =>
      assert(i.name == "querybase")
    }
  }

  test("List server nodes") {
    rpcService.rpcRx(_.FrontendApi.serverNodes()).map { lst =>
      assert(lst.size >= 2)
    }
  }
}
