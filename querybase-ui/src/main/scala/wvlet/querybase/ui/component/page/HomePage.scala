package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.frontend.ServiceApi.ServiceNode
import wvlet.querybase.ui.RPCService

/**
  */
trait HomePage extends RxElement with RPCService {

  override def render: RxElement =
    div(
      cls -> "px-1",
//      div(
//        style -> "height: calc(60vh - 60px);",
//        div("Hello Querybase")
//      ),
      table(
        cls   -> "table table-dark my-1",
        style -> "table-layout: fixed;",
        thead(
          tr(
            cls -> "bg-secondary",
            th("name"),
            th("address"),
            th("last heartbeat")
          )
        ),
        Rx.intervalMillis(1000).flatMap { i =>
            rpcRx(_.ServiceApi.serviceNodes())
          }.filter(_.isDefined).map { opt =>
            opt.map { nodeList =>
              tbody(
                nodeList.map { n =>
                  tr(
                    td(n.name),
                    td(n.address),
                    td(n.lastHeartBeatAt.toString)
                  )
                }
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
