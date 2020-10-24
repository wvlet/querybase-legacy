package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe._

/**
  *
  */
class NavBar(heightPixel: Int = 45, loginButton: LoginButton) extends RxElement {
  override def render: RxElement = {
    nav(
      cls   -> "navbar navbar-expand-md fixed-top navbar-dark bg-dark py-0",
      style -> s"height: ${heightPixel}px;",
      a(cls -> "navbar-brand", href -> "#", "Querybase"),
      button(
        cls            -> "navbar-toggler",
        tpe            -> "button",
        data("toggle") -> "collapse",
        data("target") -> "#navbarCollapse",
        aria.controls  -> "navbarCollapse",
        aria.expanded  -> "false",
        aria.label     -> "Toggle navigation",
        span(cls -> "navbar-toggler-icon")
      ),
      div(
        cls -> "collapse navbar-collapse bg-dark",
        id  -> "navbarCollapse",
        ul(
          cls -> "navbar-nav mr-auto",
          li(
            cls -> "nav-item text-nowrap",
            a(href -> "", "Menu")
          )
        )
      ),
      span(
        cls   -> "ml-2",
        style -> "color: white; ",
        loginButton
      )
    )
  }
}
