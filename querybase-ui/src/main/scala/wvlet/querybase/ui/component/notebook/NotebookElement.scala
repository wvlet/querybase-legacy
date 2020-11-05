package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.v1.code.NotebookApi.Cell
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
trait NotebookElement extends RxElement with RPCService {

  override def render: RxElement = {
    Rx.fromFuture(rpc(_.NotebookApi.getNotebook("1"))).map {
      case None => div("empty")
      case Some(notebook) =>
        div(
          h5(notebook.name),
          hr(),
          notebook.cells.zipWithIndex.map {
            case (cell, i) => new NotebookCell(i, cell)
          }
        )
    }
  }
}

class NotebookCell(index: Int, cell: Cell) extends RxElement {
  override def render: RxElement = {
    table(
      cls -> "w-100",
      tr(
        td(
          cls -> "align-top",
          small(
            cls -> "text-monospace",
            s"[${index}] "
          )
        ),
        td(
          cls -> "align-bottom",
          new TextEditor(cell.source)
        )
      )
    )
  }
}
