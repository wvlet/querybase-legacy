package wvlet.querybase.ui.component.page

import wvlet.airframe.Session
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}
import wvlet.airframe.rx.html.widget.auth.GoogleAuth
import wvlet.querybase.ui.component.{MainFrame, Navbar, RxRouter, Sidebar}

/**
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
