package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOptionVar}
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.FrontendApi.SubmitQueryRequest
import wvlet.querybase.api.frontend.code.NotebookApi.Cell
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component._
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
class NotebookEditor(rpcService: RPCService) extends RxElement with LogSupport {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private val serviceSelector = new ServiceSelector(Seq.empty)

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(
      1,
      Cell(cellType = "sql", source = "select 1", outputs = Seq("""{"text":"(query results)"}""")),
      focused = true
    )
  )

  private val updated = Rx.variable(false)

  override def render: RxElement = {
    div(
      rpcService.rpcRx(_.FrontendApi.serviceCatalog()).map { lst =>
        serviceSelector.updateList(lst)
        serviceSelector
      },
      updated.map { x =>
        cells
      }
    )
  }

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

  private def submitQuery(query: String): Unit = {
    val selectedService = serviceSelector.selectedService
    info(s"Submit to ${selectedService.name}: ${query}")
    rpcService
      .rpc(_.FrontendApi.submitQuery(SubmitQueryRequest(query = query, serviceName = selectedService.name))).map {
        resp =>
          info(s"query_id: ${resp.queryId}")
      }
  }

  class NotebookCell(val index: Int, cell: Cell, focused: Boolean = false) extends RxElement with LogSupport {

    private val result: RxOptionVar[Seq[Map[Any, Any]]] = Rx.optionVariable(None)

    private def run: Unit = {
      submitQuery(editor.getTextValue)
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
      findHTMLElement(cellId).foreach {
        _.className = "w-100 shadow-none border border-white"
      }
    }
    def focus: Unit = {
      editor.focus
      findHTMLElement(cellId).foreach {
        _.className = "w-100 shadow-sm border"
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
            onclick -> { e: MouseEvent =>
              focusOnCell(index)
            },
            td(
              cls -> "align-top bg-light",
              small(
                cls -> "text-monospace",
                new PlayIcon(onClick = { e: MouseEvent => run })
                //s"[${index}] "
              )
            ),
            td(
              cls -> "align-bottom",
              editor
            )
          ),
          new ResultWindow(cell.getOutputs)
        )
      )
    }
  }

}

class ResultWindow(output: Seq[Map[Any, Any]]) extends RxElement {
  override def render: RxElement = output.flatMap(_.get("text")).map { text =>
    tr(
      td(),
      td(
        pre(code(text.toString))
      )
    )
  }
}

class PlayIcon(onClick: MouseEvent => Unit) extends RxElement with LogSupport {

  private val baseCls = "fa fa-play-circle"

  override def render: RxElement =
    i(
      cls     -> s"${baseCls} text-secondary",
      onclick -> { e: MouseEvent => onClick(e) },
      onmouseover -> { e: MouseEvent =>
        e.getCurrentTarget.foreach { el =>
          el.className = s"${baseCls} text-info"
        }
      },
      onmouseout -> { e: MouseEvent =>
        e.getCurrentTarget.foreach {
          _.className = s"${baseCls} text-secondary"
        }
      }
    )

}
