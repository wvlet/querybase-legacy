package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.div
import wvlet.querybase.ui.component.QueryListPanel
import wvlet.querybase.ui.component.common.VerticalSplitPanel

/**
  */
class JobsPage(queryListPanel: QueryListPanel) extends RxElement {
  override def render: RxElement = new VerticalSplitPanel(
    top = queryListPanel,
    bottom = div("..."),
    ratio = 0.6
  )

}
