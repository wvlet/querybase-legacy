package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{button, cls, div, style, tpe}

/**
  */
class HomePage extends RxElement {
  override def render: RxElement =
    div(
      cls -> "px-1",
      div(
        style -> "height: calc(60vh - 60px);",
        div("Hello Trinobase")
      ),
      div(
        cls   -> "bg-dark",
        style -> "height: 40vh;",
        button(tpe -> "button", cls -> "btn btn-primary", "Primary")
      )
    )
}
