package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.code.NotebookApi.Cell
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
class NotebookEditor(rpcService: RPCService, onEnter: String => Unit) extends RxElement with LogSupport {

  private var cells: Seq[NotebookCell] = Seq.empty

  override def render: RxElement = {
    rpcService.rpcRx(_.code.NotebookApi.getNotebook("1")).map {
      case None =>
        div("N/A")
      case Some(notebook) =>
        cells = notebook.cells.zipWithIndex.map { case (cell, i) =>
          new NotebookCell(this, i + 1, cell)
        }
        div(
          h5(notebook.name),
          hr(),
          cells
        )
    }
  }

  def focusOnCell(cellIndex: Int): Unit = {
    cells.find(_.index == cellIndex).foreach { cell =>
      info(s"Focus on ${cell.index}")
      cell.focus
    }
  }

  class NotebookCell(notebook: NotebookEditor, val index: Int, cell: Cell) extends RxElement with LogSupport {
    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String =>
        onEnter(text)
      },
      onExitUp = { () =>
        info(s"Exit up cell: ${index}")
        focusOnCell((index - 1).max(0))
      },
      onExitDown = { () =>
        info(s"Exit down cell: ${index}")
        focusOnCell((index + 1).max(cells.length))
      }
    )

    def focus: Unit = {
      editor.focus
    }

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
            editor
          )
        ),
        cell.getOutputs.flatMap(output => output.get("text")).map { text =>
          tr(
            td(),
            td(
              pre(code(text.toString))
            )
          )
        }
      )
    }
  }

}
