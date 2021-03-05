package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}

class VerticalSplitPanel(top: RxElement, bottom: RxElement) extends RxElement {
  override def render: RxElement = div(
    cls -> "d-flex flex-column w-100 h-100",
    div(
      top
    ),
    div(
      cls -> "flex-grow-1 border",
      // Necessary to make w-100 work for the element with position: absolute
      style -> "position: relative;",
      div(
        cls   -> "w-100",
        style -> "position: absolute; bottom: 0;",
        bottom
      )
    )
  )
}
