package wvlet.querybase.ui.component

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport

/**
  *
  */
class LoginButton extends RxElement with LogSupport {

  override def render: RxElement = {
    LoginProfile.currentUser.transform {
      case Some(u) =>
        div(
          cls -> "dropdown",
          span(
            cls -> "d-flex flex-nowrap align-middle ml-2",
            img(
              cls    -> "rounded",
              width  -> "32px",
              height -> "32px",
              title  -> s"${u.name} ${u.email}",
              src    -> u.imageUrl
            ),
            a(
              cls            -> "btn btn-secondary btn-sm dropdown-toggle",
              href           -> "#",
              role           -> "button",
              id             -> "loginLink",
              data("toggle") -> "dropdown",
              aria.haspopup  -> true,
              aria.expanded  -> false
            ),
            div(
              cls             -> "dropdown-menu dropdown-menu-right",
              aria.labelledby -> "loginLink",
              h6(cls  -> "dropdown-header", s"${u.name}: ${u.email}"),
              div(cls -> "dropdown-divider"),
              a(
                cls  -> "dropdown-item",
                href -> "#",
                "Sign out",
                onclick -> { e: MouseEvent =>
                  LoginProfile.signOut
                }
              )
            )
          )
        )
      case None =>
        // TODO: Hide this tag while initializing Google API
        span(div(cls -> "g-signin2", data("theme") -> "dark"))
    }
  }
}
