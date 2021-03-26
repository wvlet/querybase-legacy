package wvlet.querybase.ui.component.notebook

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}
import wvlet.airframe.rx.{Rx, RxVar}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.code.NotebookApi.{Cell, Notebook}
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.querybase.ui.component._

/**
  */
class NotebookEditor(rpcService: RPCService, onEnter: String => Unit) extends RxElement with LogSupport {

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(
      1,
      Cell(cellType = "sql", source = "select 1", outputs = Seq("""{"text":"(query results)"}""")),
      focused = true
    )
//    new NotebookCell(
//      2,
//      Cell(cellType = "sql", source = "select 'a' as col1", outputs = Seq("""{"text":"(query results)"}"""))
//    )
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
    div(
      updated.map { x =>
        cells
      },
      div(
        span("console")
      )
    )
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
    cells.filter(_.index != cellIndex).foreach(_.unfocus)
  }

  class NotebookCell(val index: Int, cell: Cell, focused: Boolean = false) extends RxElement with LogSupport {

    private def run: Unit = {
      onEnter(editor.getTextValue)
      focusOnCell((index + 1).max(0), create = true)
    }

    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String => run },
      onExitUp = { () =>
        //info(s"Exit up cell: ${index}")
        focusOnCell((index - 1).max(1))
      },
      onExitDown = { () =>
        //info(s"Exit down cell: ${index}")
        focusOnCell((index + 1).min(cells.length))
      }
    )

    private val cellId = s"cell-${index}"

    def unfocus: Unit = {
      dom.document.getElementById(cellId) match {
        case e: HTMLElement =>
          e.className = "w-100 shadow-none border border-white"
      }
    }
    def focus: Unit = {
      editor.focus

      dom.document.getElementById(cellId) match {
        case e: HTMLElement =>
          e.className = "w-100 shadow-sm border"
      }
    }

    override def render: RxElement = {
      div(
        cls -> "w-100 mb-1",
        table(
          id  -> cellId,
          cls -> "w-100",
          tr(
            cls -> "mt-1",
            td(
              cls -> "align-top bg-light",
              small(
                cls -> "text-monospace",
                i(cls -> "fa fa-play-circle text-secondary", onclick -> { e: MouseEvent => run })
                //s"[${index}] "
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
      )
    }
  }

}

class PlayIcon(onClick: MouseEvent => Unit) extends RxElement {

  private val baseCls = "fa fa-play-circle"

  override def render: RxElement =
    small(
      cls -> "text-monospace",
      i(
        cls     -> s"${baseCls} text-secondary",
        onclick -> { e: MouseEvent => onClick(e) },
        onmouseout -> { e: MouseEvent =>
          e.getCurrentTarget.foreach {
            _.className = s"${baseCls} "
          }
        }
      )
    )

}
