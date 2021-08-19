package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div}

/** */
case class HStack(elems: RxElement*) extends RxElement {
  override def render: RxElement = {
    div(
      cls -> "container-fluid pl-0 pt-1",
      div(
        cls -> "d-flex flex-row justify-content-start",
        elems.map { x =>
          div(cls -> "pl-1", x)
        }
      )
    )
  }
}
