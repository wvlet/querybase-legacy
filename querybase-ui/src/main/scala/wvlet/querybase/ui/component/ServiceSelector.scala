package wvlet.querybase.ui.component

import org.scalajs.dom.raw.{Event, HTMLElement, HTMLSelectElement}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.ServiceCatalogApi.Service
import wvlet.querybase.ui.RPCService

class ServiceSelector(private var serviceList: Seq[Service], private var selectedIndex: Int = 0)
    extends RxElement
    with LogSupport {
  def updateList(newList: Seq[Service]): Unit = {
    serviceList = newList
  }

  def selectedService: Service = {
    serviceList(selectedIndex)
  }

  override def render: RxElement = form(
    cls -> "form-inline",
    div(
      cls -> "form-group",
      label(cls -> "mr-2", small("Service")),
      div(
        select(
          onchange -> { e: Event =>
            e.target match {
              case e: HTMLSelectElement =>
                info(s"Selected: ${serviceList(e.selectedIndex)}")
                selectedIndex = e.selectedIndex
              case _ =>
            }
          },
          cls -> "form-control form-control-sm",
          serviceList.map { s =>
            option(
              value -> s.id,
              s.name
            )
          }
        )
      )
    )
  )
}
