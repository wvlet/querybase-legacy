package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.VerticalSplitPanel
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
trait HomePage extends RxElement with RPCService {

  override def render: RxElement = new VerticalSplitPanel(
    top = div(
      "Hello Querybase!"
    ),
    bottom = div(
      "Information panel"
    ),
    ratio = 0.6
  )
}
