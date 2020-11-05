package wvlet.querybase.ui.component

import wvlet.airframe.bind
import wvlet.airframe.rx.{Rx, RxOptionVar}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.v1.code.NotebookApi.Notebook
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.notebook.NotebookElement

object NotebookUI {
  val targetNotebook: RxOptionVar[Notebook] = Rx.optionVariable(None)
}

/**
  */
trait NotebookUI extends RxElement {

  private val notebookElement = bind[NotebookElement]
  private val projectSelector = bind[ProjectSelector]

  override def render: RxElement = div(
    cls -> "row",
    div(
      cls -> "col-2",
      projectSelector
    ),
    div(
      cls -> "col-10 py-1",
      notebookElement
    )
  )
}
