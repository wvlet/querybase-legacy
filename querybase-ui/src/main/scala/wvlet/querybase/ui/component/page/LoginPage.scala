package wvlet.querybase.ui.component.page

import org.scalajs.dom.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.html.widget.auth.GoogleAuth

/** */
class LoginPage(auth: GoogleAuth) extends RxElement {
  override def render: RxElement = {
    div(
      cls   -> "w-100 vh-100 bg-light",
      style -> "padding-top: 100px;",
      center(
        div(
          cls   -> "card text-center",
          style -> "width: 400px;",
          div(
            cls -> "card-header",
            span(
              cls -> "align-middle"
              //img(src -> "img/logo-square-120.png", width -> 50),
            ),
            div(
              cls -> "card-body",
              center(
                p(cls -> "h5", "Querybase"),
                button(
                  id      -> "login-btn",
                  tpe     -> "button",
                  cls     -> "btn btn-primary",
                  onclick -> { e: MouseEvent => auth.signIn(uxMode = "redirect") },
                  "Sign in with Google"
                )
              )
            )
          )
        )
      )
    )
  }
}
