package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, span, style, table, tbody, td, th, thead, tr}
import wvlet.querybase.api.frontend.FrontendApi.ServerNode
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.Table

trait SystemPage extends RxElement with RPCService {

  private val nodeList = Rx.variable(Seq.empty[ServerNode])

  override def render: RxElement = {
    new Table(Seq("name", "address", "uptime"))(
      nodeList.map { lst =>
        tbody(
          lst.map { n =>
            tr(
              td(n.name),
              td(n.address),
              td(n.upTime.toString())
            )
          }
        )
      },
      repeatRpc(1500)(_.FrontendApi.serverNodes()).map { x =>
        nodeList := x
        span()
      }
    )
  }
}
