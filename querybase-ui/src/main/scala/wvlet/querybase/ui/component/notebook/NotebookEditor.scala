package wvlet.querybase.ui.component.notebook

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement, MouseEvent}
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
import scala.scalajs.js

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
        source = """-- comment
                   |select 1""".stripMargin,
        outputs = Seq("""{"text":"(query results)"}""")
      ),
      focused = true
    )
  )

  private val updated    = Rx.variable(false)
  private val schemaForm = new SchemaForm()

  override def render: RxElement = {
    // TODO: Add afterRender event hook support to airframe-rx-html
    scala.scalajs.js.timers.setTimeout(100) {
      cells.headOption.foreach {
        focusOnCell(_)
      }
    }
    div(
      div(
        cls   -> "form-row",
        style -> "min-height: 30px;",
        div(
          cls -> "col-auto",
          serviceSelector
        ),
        div(
          cls -> "col-auto",
          schemaForm
        )
      ),
      updated.map { x =>
        cells
      },
      // Add a footer-margin so that the user can scroll the last cell at the middle of the screen
      div(
        style -> "min-height: 500px;"
      )
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
    getCellIndex(cell).foreach { cellIndex =>
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

  protected def moveUp(cell: NotebookCell): Unit = {
    getCellIndex(cell).foreach { ci =>
      val swapTargetCellIndex = (ci - 1).max(0)
      if (ci != swapTargetCellIndex) {
        val newCells = Seq.newBuilder[NotebookCell]
        newCells ++= cells.slice(0, swapTargetCellIndex)
        // Swap position
        newCells += cells(ci)
        newCells += cells(swapTargetCellIndex)
        newCells ++= cells.slice(ci + 1, cells.length)
        cells = newCells.result()
        updated.forceSet(true)
      }
    }
  }

  protected def moveDown(cell: NotebookCell): Unit = {
    getCellIndex(cell).foreach { ci =>
      val swapTargetCellIndex = (ci + 1).min(cells.size - 1)
      if (ci != swapTargetCellIndex) {
        val newCells = Seq.newBuilder[NotebookCell]
        newCells ++= cells.slice(0, ci)
        // Swap position
        newCells += cells(swapTargetCellIndex)
        newCells += cells(ci)
        newCells ++= cells.slice(swapTargetCellIndex + 1, cells.length)
        cells = newCells.result()
        updated.forceSet(true)
      }
    }
  }

  private def newCell: NotebookCell = {
    new NotebookCell(UUID.randomUUID(), Cell("sql", source = "", outputs = Seq.empty))
  }

  protected def insertCellAfter(cell: NotebookCell): NotebookCell = {
    val targetCellIndex = getCellIndex(cell)
    val ci              = targetCellIndex.map(_ + 1).getOrElse(cells.size).min(cells.size)
    val nc              = newCell
    val newCells        = Seq.newBuilder[NotebookCell]
    newCells ++= cells.slice(0, ci)
    newCells += nc
    newCells ++= cells.slice(ci, cells.size)
    cells = newCells.result()
    updated.forceSet(true)
    nc
  }

  /** Submit a query and get a query Id
    */
  private def submitQuery(query: String): Future[String] = {
    serviceSelector.getSelectedService match {
      case Some(selectedService) =>
        info(s"Submit to ${selectedService.name}: ${query}")
        rpcClient.FrontendApi
          .submitQuery(
            SubmitQueryRequest(query = query, serviceName = selectedService.name, schema = schemaForm.getText)
          ).map { resp =>
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
        showResult := true
        currentQueryInfo := None
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
          getCell(cellIndex + 1).foreach { cell =>
            focusOnCell(cell)
          }
        }
      }
    )

    private val cellIdStr    = s"cell-${cellId}"
    private val resultCellId = s"${cellId}-result"

    private val defaultStyle = "w-100 shadow-none border border-white"
    private val focusedStyle = "w-100 shadow-sm border border-info"

    private var hasFocus = false

    def unfocus: Unit = {
      findHTMLElement(cellIdStr).foreach {
        _.className = defaultStyle
      }
      hasFocus = false
    }
    def focus: Unit = {
      if (!hasFocus) {
        editor.focus
        findHTMLElement(cellIdStr).foreach { el =>
          el.className = focusedStyle
        }
      }
      hasFocus = true
    }

    private val showResult = Rx.variable(true)

    def menuIcon(faStyle: String) = small(
      i(
        cls   -> s"fa ${faStyle} mr-1 text-secondary",
        style -> "width: 11px;"
      )
    )
    def dropdownItem = a(
      cls -> "dropdown-item px-3"
    )

    private val visible = Rx.variable(false)

    class CellOpsIcons extends RxElement {
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
              val newCell = insertCellAfter(thisCell)
              focusOnCell(newCell)
            }
          )
        )
    }

    private val cellOpsIcon = new CellOpsIcons

    private val cellMenuStyle =
      "background: #ffffff; display: flex; position: absolute; top: -20px; right: 8px; z-index: 1070;"

    def cellMenuIcon(name: String, faStyle: String, onClick: MouseEvent => Unit) = {
      val baseStyle = s"fa ${faStyle} mr-1 p-1"
      i(
        title   -> name,
        cls     -> s"${baseStyle} text-secondary",
        onclick -> { e: MouseEvent => onClick(e) },
        onmouseover -> { e: MouseEvent =>
          e.getCurrentTarget.foreach { el =>
            el.className = s"${baseStyle} text-white bg-info"
          }
        },
        onmouseout -> { e: MouseEvent =>
          e.getCurrentTarget.foreach { el =>
            el.className = s"${baseStyle} text-secondary"
          }
        }
      )
    }

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
              // This setting is necessary for placing cell menu icons at the right-top corner
              style -> "display: flex; flex-direction: column; position: relative; ",
              editor,
              div(
                visible.map {
                  case true =>
                    style -> s"visibility: visible; ${cellMenuStyle}"
                  case false =>
                    style -> s"visibility: hidden; ${cellMenuStyle}"
                },
                cls -> "border rounded shadow px-2",
                span(
                  cellMenuIcon(
                    name = "Run Cell",
                    "fa-play",
                    { e: MouseEvent =>
                      run
                    }
                  ),
                  cellMenuIcon(
                    "Copy query to clipboard",
                    "fa-clipboard",
                    { e: MouseEvent =>
                      val clipboardText = new StringBuilder()
                      clipboardText.append(s"${editor.getTextValue}")
                      js.Dynamic.global.navigator.clipboard.writeText(clipboardText.result())
                    }
                  ),
                  cellMenuIcon(
                    "Copy query and results to clipboard",
                    "fa-copy",
                    { e: MouseEvent =>
                      val clipboardText = new StringBuilder()
                      clipboardText.append(s"${editor.getTextValue}\n\n")
                      currentQueryInfo.get.flatMap { qi =>
                        qi.result.map { result =>
                          clipboardText.append(QueryResultPrinter.print(result))
                        }
                      }
                      js.Dynamic.global.navigator.clipboard.writeText(clipboardText.result())
                    }
                  ),
                  cellMenuIcon(
                    name = "Move Up",
                    "fa-angle-up",
                    { e: MouseEvent =>
                      moveUp(thisCell)
                    }
                  ),
                  cellMenuIcon(
                    "Move Down",
                    "fa-angle-down",
                    { e: MouseEvent =>
                      moveDown(thisCell)
                    }
                  ),
                  cellMenuIcon(
                    "Delete",
                    "fa-trash",
                    { e: MouseEvent =>
                      deleteCell(thisCell)
                    }
                  ),
                  cellMenuIcon(
                    "Fold/Unfold",
                    "fa-caret-down",
                    { e: MouseEvent =>
                      showResult.update(prev => !prev)
                    }
                  ),
                  cellMenuIcon(
                    "Menu",
                    "fa-ellipsis-v",
                    onClick = { e: MouseEvent => }
                  )(
                    id             -> s"dropdown-${cellId}",
                    aria.haspopup  -> true,
                    aria.expanded  -> false,
                    data("toggle") -> "dropdown"
                  ),
                  span(
                    id  -> s"dropdown-menu-${cellId}",
                    cls -> "dropdown-menu",
                    // Need to set a higher z-index than query result table header (1020)
                    style           -> "z-index: 1070; ",
                    aria.labelledby -> s"dropdown-${cellId}",
                    dropdownItem(
                      menuIcon("fa-menu"),
                      "More menu",
                      onclick -> { e: MouseEvent =>
                        // TODO
                      }
                    )
                  )
                )
              )
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
                      Rx.intervalMillis(800)
                        .flatMap { _ =>
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
