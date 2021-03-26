package wvlet.querybase.ui.component.page

import wvlet.airframe._
import wvlet.airframe.rx.html.RxElement
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.notebook.NotebookEditor
import wvlet.querybase.ui.component.{QueryListPanel, VerticalSplitPanel}

/**
  */
trait ExplorePage extends RxElement {
  private val rpcService     = bind[RPCService]
  private val queryListPanel = bind[QueryListPanel]

  private val notebookEditor = new NotebookEditor(rpcService)

  override def render: RxElement = {
    new VerticalSplitPanel(
      top = notebookEditor,
      bottom = queryListPanel,
      ratio = 0.7
    )
  }
}
