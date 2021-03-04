package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
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
      table(
        cls -> "table",
        thead(
          th("name"),
          th("address"),
          th("last heartbeat")
        ),
        rpcRx(_.ServiceApi.serviceNodes()).map { nodeList =>
          nodeList.map { n =>
            tr(
              td(n.name),
              td(n.address),
              td(n.lastHeartBeatAt.toString)
            )
          }
        }
      ),
      div(
        cls   -> "bg-dark",
        style -> "height: 40vh;",
        button(tpe -> "button", cls -> "btn btn-primary", "Primary")
      )
    )
}
