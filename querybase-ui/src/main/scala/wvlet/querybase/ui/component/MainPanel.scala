package wvlet.querybase.ui.component

import wvlet.airframe._
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.notebook.NotebookFrame

/**
  */
trait MainPanel extends RxElement {

  private val navbar     = bind[NavBar]
  private val notebookUI = bind[NotebookFrame]

  override def render = {
    div(
      navbar,
      div(
        cls   -> "container-fluid",
        style -> """padding-top: 40px; """,
        notebookUI
      )
    )
  }
}
