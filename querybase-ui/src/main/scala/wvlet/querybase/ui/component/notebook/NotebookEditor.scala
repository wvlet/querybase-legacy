package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.{Rx, RxVar}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.code.NotebookApi.{Cell, Notebook}
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
class NotebookEditor(rpcService: RPCService, onEnter: String => Unit) extends RxElement with LogSupport {

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(1, Cell(cellType = "sql", source = "select 1", outputs = Seq("""{"text":"(query results)"}"""))),
    new NotebookCell(
      2,
      Cell(cellType = "sql", source = "select 'a' as col1", outputs = Seq("""{"text":"(query results)"}"""))
    )
  )

  private val updated = Rx.variable(false)

  private val notebook: Notebook = Notebook(
    id = "1",
    name = "my notebook",
    description = "",
    cells = Seq(
      Cell("sql", source = "select 100, 'a'")
    )
  )

  override def render: RxElement = {
    updated.map { x =>
      cells
    }
  }

//    rpcService.rpcRx(_.code.NotebookApi.getNotebook("1")).map {
//      case None =>
//        div("N/A")
//      case Some(notebook) =>
//        cells = notebook.cells.zipWithIndex.map { case (cell, i) =>
//          new NotebookCell(i + 1, cell)
//        }
//        div(
//          h5(notebook.name),
//          hr(),
//          cells
//        )
//    }

  protected def focusOnCell(cellIndex: Int, create: Boolean = false): Unit = {
    if (create && cellIndex - 1 >= cells.size) {
      cells =
        cells :+ new NotebookCell(cellIndex, Cell("sql", source = "", outputs = Seq("""{"text":"(query results)"}""")))
      updated.forceSet(true)
    }
    cells.find(_.index == cellIndex).foreach { cell =>
      info(s"Focus on ${cell.index}")
      cell.focus
    }
  }

  class NotebookCell(val index: Int, cell: Cell) extends RxElement with LogSupport {
    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String =>
        onEnter(text)
        focusOnCell((index + 1).max(0), create = true)
      },
      onExitUp = { () =>
        info(s"Exit up cell: ${index}")
        focusOnCell((index - 1).max(0))
      },
      onExitDown = { () =>
        info(s"Exit down cell: ${index}")
        focusOnCell((index + 1).min(cells.length))
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
