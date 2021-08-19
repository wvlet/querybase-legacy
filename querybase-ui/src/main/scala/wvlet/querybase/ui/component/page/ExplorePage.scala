package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.common.{FixedHeightFrame, VStack}
import wvlet.querybase.ui.component.explore.ExploreWindow

/** */
class ExplorePage(exploreWindow: ExploreWindow) extends RxElement with RPCQueue {
  override def render: RxElement = FixedHeightFrame(
    exploreWindow
  )
}
