package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}

class VerticalSplitPanel(top: RxElement, bottom: RxElement, ratio: Double = 0.5) extends RxElement {

  override def render: RxElement = div(
    cls -> "d-flex flex-column w-100 h-100",
    div(
      cls   -> "border overflow-auto",
      style -> s"height: ${ratio * 100}%;",
      top
    ),
    div(
      cls   -> "border overflow-auto",
      style -> s"height: ${(1 - ratio) * 100}%;",
      bottom
    )
  )
}
