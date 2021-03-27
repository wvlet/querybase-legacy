package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.querybase.ui.component.notebook.NotebookEditor

/**
  */
class ExplorePage(notebookEditor: NotebookEditor) extends RxElement {
  override def render: RxElement = {
    notebookEditor
  }
}
