package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div}
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.airframe._
import wvlet.querybase.ui.component.notebook.NotebookFrame

/**
  */
trait NotebookPage extends RxElement {
  private val frame    = bind[NotebookFrame]
  private val notebook = new EditorCell()

  override def render: RxElement = div(
    cls -> "p-1",
    frame,
    notebook
  )
}

class EditorCell extends RxElement {
  private val editor = new TextEditor(initialValue = "select 1")

  override def render: RxElement = div(
    div(
      cls -> "py-1 w-100",
      editor,
      div(cls -> "btn btn-sm btn-secondary", "run")
    )
  )
}
