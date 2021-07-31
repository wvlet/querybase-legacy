package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{div, style}
import wvlet.querybase.ui.component.Navbar
import wvlet.querybase.ui.component.common.FixedHeightFrame
import wvlet.querybase.ui.component.notebook.NotebookEditor

/**
  */
class NotebookPage(notebookEditor: NotebookEditor) extends RxElement {
  override def render: RxElement = {
    FixedHeightFrame(
      notebookEditor
    )
  }
}
