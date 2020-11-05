package wvlet.querybase.ui.component

import wvlet.airframe.bind
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.notebook.Notebook

/**
  */
trait NotebookUI extends RxElement {

  private val projectSelector = bind[ProjectSelector]

  override def render: RxElement = div(
    cls -> "row",
    div(
      cls -> "col-2",
      projectSelector
    ),
    div(
      cls -> "col-10",
      new Notebook()
    )
  )
}
