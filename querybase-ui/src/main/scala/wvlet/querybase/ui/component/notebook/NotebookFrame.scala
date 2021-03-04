package wvlet.querybase.ui.component.notebook

import wvlet.airframe.bind
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOptionVar}
import wvlet.querybase.api.frontend.code.NotebookApi.Notebook
import wvlet.querybase.ui.component.ProjectSelector

object NotebookFrame {
  val targetNotebook: RxOptionVar[Notebook] = Rx.optionVariable(None)
}

/**
  */
trait NotebookFrame extends RxElement {
  private val projectSelector = bind[ProjectSelector]
  private val notebookEditor  = bind[NotebookEditor]

  override def render: RxElement = div(
    cls -> "w-100",
    projectSelector,
    notebookEditor
  )
}
