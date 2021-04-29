package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.Navbar
import wvlet.querybase.ui.component.explore.ExploreWindow

/**
  */
class ExplorePage(exploreWindow: ExploreWindow) extends RxElement with RPCQueue {
  override def render: RxElement =
    div(
      style -> s"height: calc(100vh - ${Navbar.navbarHeight}px); overflow-y: auto;",
      exploreWindow
    )
}
