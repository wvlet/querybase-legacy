package wvlet.querybase.ui.component

import wvlet.airframe.Session
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}

class MainFrame(topbar: Navbar, router: RxRouter, sidebar: Sidebar, session: Session) extends RxElement {
  override def render: RxElement = div(
    cls -> "d-flex w-100",
    sidebar,
    div(
      cls -> "flex-column flex-fill flex-grow-1 flex-shrink-1",
      topbar,
      div(
        style -> s"height: calc(100vh - ${Navbar.navbarHeight}px); width: 100%;",
        router.current.transform {
          case Some(m) => session.getInstanceOf(m.route.pageSurface).asInstanceOf[RxElement]
          case None    => div("N/A")
        }
      )
    )
  )
}
