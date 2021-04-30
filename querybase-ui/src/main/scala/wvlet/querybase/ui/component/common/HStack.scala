package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div}

/**
  */
case class HStack(elems: RxElement*) extends RxElement {
  override def render: RxElement = {
    div(
      cls -> "container-fluid",
      div(
        cls -> "row",
        elems.map { x =>
          div(cls -> "col", x)
        }
      )
    )
  }
}
