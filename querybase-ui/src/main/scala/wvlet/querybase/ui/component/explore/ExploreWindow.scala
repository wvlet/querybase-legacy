package wvlet.querybase.ui.component.explore

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.common.VStack

/**
  */
class ExploreWindow(searchBox: ExploreSearchBox) extends RxElement with LogSupport {

  override def render: RxElement = {
    VStack(
      div(
        cls -> "p-1",
        searchBox
      ),
      div(
        "editor"
      )
    )
  }
}
