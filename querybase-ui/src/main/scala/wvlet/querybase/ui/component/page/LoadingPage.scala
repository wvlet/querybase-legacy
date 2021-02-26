package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, role, span, style}

/**
  */
object LoadingPage extends RxElement {
  override def render: RxElement = div(
    cls   -> "w-100 vh-100 bg-light",
    style -> "padding-top: 100px",
    div(
      cls -> "text-center",
      div(
        cls  -> "spinner-border text-primary",
        role -> "status",
        span(cls -> "sr-only", "Loading...")
      ),
      div(cls -> "text-secondary", "Loading ...")
    )
  )
}
