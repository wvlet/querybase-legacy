package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

/**
  */
class Navbar(loginProfile: LoginProfileIcon, router: RxRouter) extends RxElement {

  override def render: RxElement = {
    nav(
      cls   -> "navbar navbar-dark bg-dark sticky-top",
      style -> "height: 60px;",
      span(
        cls -> "navbar-brand",
        router.current.map { p => p.route.title }
      ),
      //      form(
      //        input(cls  -> "form-control mr-2", tpe -> "search", placeholder -> "Search", aria.label -> "Search"),
      //        button(cls -> "btn btn-success", tpe   -> "submit", "Search")
      //      ),
      //      span("Version: xxx"),
      loginProfile
    )
  }
}
