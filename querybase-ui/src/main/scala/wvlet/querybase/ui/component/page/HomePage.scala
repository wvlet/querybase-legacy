package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{button, cls, div, span, style, tpe}
import wvlet.querybase.ui.RPCService

/**
  */
trait HomePage extends RxElement with RPCService {

  override def render: RxElement =
    div(
      cls -> "px-1",
      div(
        style -> "height: calc(60vh - 60px);",
        div("Hello Querybase")
      ),
      rxRpc(_.ServiceApi.serviceNodes()).map { nodeList =>
        nodeList.map { n =>
          div(
            span(s"${n.name}: ${n.address} ${n.lastHeartBeatAt.toString}")
          )
        }
      },
      div(
        cls   -> "bg-dark",
        style -> "height: 40vh;",
        button(tpe -> "button", cls -> "btn btn-primary", "Primary")
      )
    )
}
