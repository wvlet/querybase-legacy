package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.Table

class ServicePage(rpcService: RPCService) extends RxElement {
  override def render: RxElement = div(
    new Table(Seq("name", "type", "description"))(
      tbody(
        rpcService.rpcRx(_.ServiceApi.serviceCatalog()).map { lst =>
          lst.map { s =>
            tr(
              td(s.name),
              td(s.serviceType),
              td(s.description)
            )
          }
        }
      )
    )
  )
}
