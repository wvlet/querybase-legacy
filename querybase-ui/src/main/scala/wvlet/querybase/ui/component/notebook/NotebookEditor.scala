package wvlet.querybase.ui.component.notebook

import org.scalajs.dom
import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOptionVar}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.FrontendApi.SubmitQueryRequest
import wvlet.querybase.api.frontend.code.NotebookApi.Cell
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.ui.component._
import wvlet.querybase.ui.component.editor.TextEditor

import scala.concurrent.Future
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("jquery", JSImport.Namespace)
object jquery extends JQueryStatic

/**
  */
class NotebookEditor(serviceSelector: ServiceSelector, rpcRxClient: ServiceJSClientRx, rpcClient: ServiceJSClient)
    extends RxElement
    with LogSupport {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(
      1,
      Cell(
        cellType = "sql",
        source = "select 1",
        outputs = Seq("""{"text":"(query results)"}""")
      ),
      focused = true
    )
  )

  private val updated = Rx.variable(false)

  override def render: RxElement = {
    div(
      div(
        style -> "min-height: 30px;",
        serviceSelector
      ),
      div(
        new EditorIcon(
          name = "Add Cell",
          "fa-plus",
          onClick = { e: MouseEvent =>
            focusOnCell((cells.size + 1).max(0), create = true)
          }
        ),
        new EditorIcon(
          name = "Delete Cell",
          "fa-cut",
          onClick = { e: MouseEvent => }
        ),
        new EditorIcon(
          name = "Run Cell",
          "fa-play",
          onClick = { e: MouseEvent => }
        )
      ),
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

  /** Submit a query and get a query Id
    */
  private def submitQuery(query: String): Future[String] = {
    serviceSelector.getSelectedService match {
      case Some(selectedService) =>
        info(s"Submit to ${selectedService.name}: ${query}")
        rpcClient.FrontendApi.submitQuery(SubmitQueryRequest(query = query, serviceName = selectedService.name)).map {
          resp =>
            info(s"query_id: ${resp.queryId}")
            resp.queryId
        }
      case None =>
        Future.failed(new IllegalStateException("No service is selected"))
    }
  }

  class NotebookCell(val index: Int, cell: Cell, focused: Boolean = false) extends RxElement with LogSupport {

    private val currentQueryId                           = Rx.optionVariable[String](None)
    private val currentQueryInfo: RxOptionVar[QueryInfo] = Rx.optionVariable(None)

    private def run: Unit = {
      submitQuery(editor.getTextValue).foreach { queryId =>
        currentQueryId := Some(queryId)
        currentQueryInfo := None
      }
    }

    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String =>
        if (text.trim.nonEmpty) {
          showResult := true
          run
        }
      },
      onExitUp = { () =>
        //info(s"Exit up cell: ${index}")
        focusOnCell((index - 1).max(1))
      },
      onExitDown = { () =>
        //info(s"Exit down cell: ${index}")
        focusOnCell((index + 1).min(cells.length))
      }
    )

    private val cellId       = s"cell-${index}"
    private val resultCellId = s"${cellId}-result"

    private val defaultStyle = "w-100 shadow-none border border-white"
    private val focusedStyle = "w-100 shadow-sm border"

    def unfocus: Unit = {
      findHTMLElement(cellId).foreach {
        _.className = defaultStyle
      }
    }
    def focus: Unit = {
      editor.focus
      findHTMLElement(cellId).foreach {
        _.className = focusedStyle
      }
    }

    private val showResult = Rx.variable(true)

    override def render: RxElement = {
      div(
        cls -> "w-100",
        table(
          id  -> cellId,
          cls -> defaultStyle,
          onclick -> { e: MouseEvent =>
            focusOnCell(index)
          },
          tr(
            cls -> "mt-1",
            td(
              cls -> "align-top bg-light",
              span(
                new EditorIcon(
                  "Fold",
                  "fa-caret-down",
                  onClick = { e: MouseEvent =>
                    showResult.update(prev => !prev)
                  }
                )
              )
            ),
            td(
              cls -> "align-middle",
              editor
            )
          ),
          tr(
            td(
              cls -> "align-top bg-light"
            ),
            td(
              div(
                showResult.map {
                  case true =>
                    cls -> "collapse show"
                  case false =>
                    cls -> "collapse hide"
                },
                id    -> s"${resultCellId}",
                style -> "min-height: 22px; ",
                Rx.join(currentQueryInfo, currentQueryId).map[RxElement] {
                  case (Some(qi), _) =>
                    new QueryStatusLine(Some(qi))
                  case (None, Some(queryId)) =>
                    span(
                      Rx.intervalMillis(800).flatMap { i =>
                          rpcRxClient.FrontendApi
                            .getQueryInfo(queryId)
                            .map {
                              case Some(qi) =>
                                if (qi.queryStatus.isFinished) {
                                  currentQueryInfo := Some(qi)
                                }
                                new QueryStatusLine(Some(qi))
                              case None =>
                                small("Query not found")
                            }
                        }.startWith(small("Loading ..."))
                    )
                  case (None, None) =>
                    span()
                }
              )
            )
          )
        )
      )
    }
  }
}
