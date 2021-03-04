package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.RPCService

/**
  */
trait HomePage extends RxElement with RPCService {

  override def render: RxElement =
    div(
      table(
        cls   -> "table table-dark my-1",
        style -> "table-layout: fixed;",
        thead(
          tr(
            cls -> "bg-secondary",
            th("name"),
            th("address"),
            th("uptime")
          )
        ),
        repeatRpc(1000)(_.ServiceApi.serviceNodes()).map { nodeList =>
          tbody(
            nodeList.map { n =>
              tr(
                td(n.name),
                td(n.address),
                td(n.upTime.toString())
              )
            }
          )
        }
      )
    )
}
