package wvlet.querybase.ui.component.page

import wvlet.airframe.Session
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}
import wvlet.airframe.rx.html.widget.auth.GoogleAuth
import wvlet.querybase.ui.component.{Navbar, RxRouter, Sidebar}

/**
 *
 */
class MainPage(mainFrame: MainFrame, loginMenu: LoginPage, auth: GoogleAuth) extends RxElement {

  override def render: RxElement = {
    auth.init.transform[RxElement] {
      case None =>
        LoadingPage
      case _ =>
        auth.getCurrentUser.transform {
          case Some(user) =>
            mainFrame
          case None =>
            loginMenu
        }
    }
  }
}

class MainFrame(topbar: Navbar, router: RxRouter, sidebar: Sidebar, session: Session) extends RxElement {
  override def render: RxElement = div(
    cls -> "d-flex w-100",
    sidebar,
    div(
      cls -> "flex-column flex-fill flex-grow-1 flex-shrink-1",
      topbar,
      div(
        style -> "height: calc(100vh - 60px); width: 100%;",
        router.current.transform {
          case Some(m) => session.getInstanceOf(m.route.pageSurface).asInstanceOf[RxElement]
          case None    => div("N/A")
        }
      )
    )
  )
}
