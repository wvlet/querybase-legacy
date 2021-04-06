package wvlet.querybase.ui.component.notebook

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}
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

import java.util.UUID
import scala.concurrent.Future

/**
  */
class NotebookEditor(serviceSelector: ServiceSelector, rpcRxClient: ServiceJSClientRx, rpcClient: ServiceJSClient)
    extends RxElement
    with LogSupport {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(
      UUID.randomUUID(),
      Cell(
        cellType = "sql",
        source = "show functions",
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

  protected def focusOnCell(cell: NotebookCell): Unit = {
    cells.foreach { c =>
      if (c eq cell) {
        c.focus
      } else {
        c.unfocus
      }
    }
  }

  protected def getCell(index: Int): Option[NotebookCell] = {
    if (index >= 0 && index < cells.length) {
      Option(cells(index))
    } else {
      None
    }
  }
  protected def getCellIndex(cell: NotebookCell): Option[Int] = {
    cells.zipWithIndex.find { case (c, i) => c eq cell }.map(_._2)
  }

  protected def deleteCell(cell: NotebookCell): Unit = {
    getCellIndex(cell: NotebookCell).foreach { cellIndex =>
      val newCells = Seq.newBuilder[NotebookCell]
      newCells ++= cells.slice(0, cellIndex)
      newCells ++= cells.slice(cellIndex + 1, cells.size)
      cells = newCells.result()

      if (cells.isEmpty) {
        cells = Seq(newCell)
      }
      updated.forceSet(true)
    }
  }

  private def newCell: NotebookCell = {
    new NotebookCell(UUID.randomUUID(), Cell("sql", source = "", outputs = Seq.empty))
  }

  protected def insertCellAfter(cell: NotebookCell): Unit = {
    val targetCellIndex = getCellIndex(cell)
    val ci              = targetCellIndex.map(_ + 1).getOrElse(cells.size).min(cells.size)
    val newCells        = Seq.newBuilder[NotebookCell]
    newCells ++= cells.slice(0, ci)
    newCells += newCell
    newCells ++= cells.slice(ci, cells.size)
    cells = newCells.result()
    updated.forceSet(true)
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

  class NotebookCell(cellId: UUID, cell: Cell, focused: Boolean = false) extends RxElement with LogSupport {
    thisCell =>

    private val currentQueryId                           = Rx.optionVariable[String](None)
    private val currentQueryInfo: RxOptionVar[QueryInfo] = Rx.optionVariable(None)

    private def run: Unit = {
      submitQuery(editor.getTextValue).foreach { queryId =>
        currentQueryId := Some(queryId)
        currentQueryInfo := None
        showResult := true
      }
    }

    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String =>
        if (text.trim.nonEmpty) {
          run
          focusOnCell(thisCell)
        }
      },
      onExitUp = { () =>
        getCellIndex(thisCell).foreach { cellIndex =>
          getCell(cellIndex - 1).foreach(focusOnCell(_))
        }
      },
      onExitDown = { () =>
        getCellIndex(thisCell).foreach { cellIndex =>
          getCell(cellIndex + 1).foreach(focusOnCell(_))
        }
      }
    )

    private val cellIdStr    = s"cell-${cellId}"
    private val resultCellId = s"${cellId}-result"

    private val defaultStyle = "w-100 shadow-none border border-white"
    private val focusedStyle = "w-100 shadow-sm border border-info"

    def unfocus: Unit = {
      findHTMLElement(cellIdStr).foreach {
        _.className = defaultStyle
      }
    }
    def focus: Unit = {
      editor.focus
      findHTMLElement(cellIdStr).foreach {
        _.className = focusedStyle
      }
    }

    private val showResult = Rx.variable(true)

    class CellOpsIcons extends RxElement {
      private val visible = Rx.variable(false)

      def setVisibility(isVisible: Boolean): Unit = {
        visible := isVisible
      }

      override def render: RxElement =
        span(
          cls -> "dropdown text-nowrap",
          visible.map {
            case true =>
              style -> s"visibility: visible;"
            case false =>
              style -> s"visibility: hidden;"
          },
          new EditorIcon(
            "Add a new cell",
            "fa-plus",
            onClick = { e: MouseEvent =>
              insertCellAfter(thisCell)
            }
          ),
          span(
            id             -> s"dropdown-${cellId}",
            aria.haspopup  -> true,
            aria.expanded  -> false,
            data("toggle") -> "dropdown",
            new EditorIcon(
              "Fold",
              "fa-caret-down",
              onClick = { e: MouseEvent =>
                //showResult.update(prev => !prev)
              }
            ) {
              onmouseover -> { e: MouseEvent =>
                dom.document.getElementById(s"dropdown-menu-${cellId}") match {
                  case e: HTMLElement =>
                    e.style = "display: block;"
                  case _ =>
                }
              }
            }
          ),
          span(
            id  -> s"dropdown-menu-${cellId}",
            cls -> "dropdown-menu",
            // Need to set a higher z-index than query result table header (1020)
            style           -> "z-index: 1070; ",
            aria.labelledby -> s"dropdown-${cellId}",
            a(
              cls -> "dropdown-item",
              "Fold/Unfold",
              onclick -> { e: MouseEvent =>
                showResult.update(prev => !prev)
              }
            ),
            a(
              cls -> "dropdown-item",
              "Delete",
              onclick -> { e: MouseEvent =>
                deleteCell(thisCell)
              }
            ),
            a(
              cls -> "dropdown-item",
              "Run Cell",
              onclick -> { e: MouseEvent =>
                run
              }
            ),
            a(cls -> "dropdown-item", "Move Up"),
            a(cls -> "dropdown-item", "Move Down")
          )
        )
    }

    private val cellOpsIcon = new CellOpsIcons

    override def render: RxElement = {
      div(
        cls -> "w-100",
        table(
          id  -> cellIdStr,
          cls -> defaultStyle,
          onclick -> { e: MouseEvent =>
            focusOnCell(thisCell)
          },
          onmouseover -> { e: MouseEvent =>
            cellOpsIcon.setVisibility(true)
          },
          onmouseout -> { e: MouseEvent =>
            cellOpsIcon.setVisibility(false)
          },
          tr(
            cls -> "mt-1",
            td(
              cls -> "align-top bg-light",
              cellOpsIcon
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
