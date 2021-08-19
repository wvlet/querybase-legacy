package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{div, style}
import wvlet.querybase.ui.component.Navbar

/** */
object FixedHeightFrame extends RxElement {
  override def render: RxElement = {
    div(
      style -> s"height: calc(100vh - ${Navbar.navbarHeight}px); overflow-y: auto;"
    )
  }
}
