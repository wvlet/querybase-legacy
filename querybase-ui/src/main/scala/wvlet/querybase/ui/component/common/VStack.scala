package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, table, tr}

/**
  */
case class VStack(elems: RxElement*) extends RxElement {
  override def render: RxElement = div(
    table(
      cls -> "w-100",
      elems.map { row =>
        tr(row)
      }
    )
  )
}
