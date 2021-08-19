package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, table, td, tr}

/** */
case class VStack(elems: RxElement*) extends RxElement {
  override def render: RxElement = div(
    div(
      cls -> "container-fluid",
      elems.map { row =>
        div(
          cls -> "row",
          row
        )
      }
    )
  )
}
