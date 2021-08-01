package wvlet.querybase.ui.component.explore

import wvlet.airframe.rx.html.RxElement
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.ServiceSelector
import wvlet.querybase.ui.component.common.{HStack, VStack}

/**
  */
class ExploreWindow(searchBox: ExploreSearchBox, serviceSelector: ServiceSelector) extends RxElement with LogSupport {

  private val queryEditor = new QueryEditor()

  override def render: RxElement = {
    VStack(
      HStack(
        searchBox
      ),
      queryEditor
    )
  }
}
