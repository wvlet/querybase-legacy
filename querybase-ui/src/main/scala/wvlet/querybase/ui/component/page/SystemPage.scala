package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.{Rx, RxStream}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, span, style, table, tbody, td, th, thead, tr}
import wvlet.querybase.api.frontend.FrontendApi.ServerNode
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.common.Table

import java.util.concurrent.TimeUnit

class SystemPage(rpcService: RPCService) extends RxElement {

  private val nodeList: RxStream[Seq[ServerNode]] =
    rpcService
      .repeatRpc(1500, TimeUnit.MILLISECONDS) {
        _.FrontendApi.serverNodes()
      }
      .cache

  override def render: RxElement = {
    new Table(Seq("name", "address", "uptime"))(
      nodeList.map { lst =>
        lst.map { n =>
          tr(
            td(n.name),
            td(n.address),
            td(n.upTime.toString())
          )
        }
      }
    )
  }
}
