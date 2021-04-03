package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.notebook.NotebookEditor

/**
  */
class ExplorePage(notebookEditor: NotebookEditor) extends RxElement {
  override def render: RxElement = {
    div(
      style -> "height: calc(100vh - 60px); overflow-y: scroll;",
      notebookEditor
    )
  }
}
