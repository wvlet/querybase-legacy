package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Cancelable, Rx, RxOptionVar, RxVar}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.FrontendApi.{
  NotebookCellData,
  NotebookData,
  NotebookSession,
  SaveNotebookRequest,
  SubmitQueryRequest
}
import wvlet.querybase.api.frontend.code.NotebookApi.Cell
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.ui.component._
import wvlet.querybase.ui.component.common.Clipboard
import wvlet.querybase.ui.component.editor.TextEditor

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.scalajs.js.timers.SetIntervalHandle
import scala.util.Try

/**
  */
class NotebookEditor(
    serviceSelector: ServiceSelector,
    private[notebook] val rpcRxClient: ServiceJSClientRx,
    rpcClient: ServiceJSClient
) extends RxElement
    with LogSupport {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(
      this,
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

  private val notebookSaver = new NotebookSaver(this, rpcRxClient)

  override def beforeRender: Unit = {
    notebookSaver.start
  }

  override def beforeUnmount: Unit = {
    notebookSaver.stop
  }

  def getNotebookData: NotebookData = {
    val cellData = cells.map { c =>
      NotebookCellData(
        text = c.getTextValue,
        queryInfo = c.getQueryInfo
      )
    }
    NotebookData(cellData)
  }

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

  def focusOnCell(cell: NotebookCell): Unit = {
    cells.foreach { c =>
      if (c eq cell) {
        c.focus
      } else {
        c.unfocus
      }
    }
  }

  def getCell(index: Int): Option[NotebookCell] = {
    if (index >= 0 && index < cells.length) {
      Option(cells(index))
    } else {
      None
    }
  }

  def getCellIndex(cell: NotebookCell): Option[Int] = {
    cells.zipWithIndex.find { case (c, i) => c eq cell }.map(_._2)
  }

  def deleteCell(cell: NotebookCell): Unit = {
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

  def moveUp(cell: NotebookCell): Unit = {
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

  def moveDown(cell: NotebookCell): Unit = {
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
    new NotebookCell(this, UUID.randomUUID(), Cell("sql", source = "", outputs = Seq.empty))
  }

  def insertCellAfter(cell: NotebookCell): NotebookCell = {
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
  private[notebook] def submitQuery(query: String): Future[String] = {
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

}

class NotebookCell(val notebookEditor: NotebookEditor, cellId: UUID, cell: Cell, focused: Boolean = false)
    extends RxElement
    with LogSupport {
  thisCell =>

  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private val currentQueryId                           = Rx.optionVariable[String](None)
  private val currentQueryInfo: RxOptionVar[QueryInfo] = Rx.optionVariable(None)

  def runCell: Unit = {
    notebookEditor.submitQuery(editor.getTextValue).foreach { queryId =>
      currentQueryId := Some(queryId)
      showResult := true
      currentQueryInfo := None
    }
  }

  private val editor = new TextEditor(
    initialValue = cell.source,
    onEnter = { text: String =>
      if (text.trim.nonEmpty) {
        runCell
        notebookEditor.focusOnCell(thisCell)
      }
    },
    onExitUp = { () =>
      notebookEditor.getCellIndex(thisCell).foreach { cellIndex =>
        notebookEditor.getCell(cellIndex - 1).foreach(notebookEditor.focusOnCell(_))
      }
    },
    onExitDown = { () =>
      notebookEditor.getCellIndex(thisCell).foreach { cellIndex =>
        notebookEditor.getCell(cellIndex + 1).foreach { cell =>
          notebookEditor.focusOnCell(cell)
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

  def getTextValue: String = {
    editor.getTextValue
  }

  def getQueryInfo: Option[QueryInfo] = {
    currentQueryInfo.get
  }

  def toggleResultView: Unit = {
    showResult.update(prev => !prev)
  }

  private[notebook] val showResult       = Rx.variable(true)
  private[notebook] val isToolbarVisible = Rx.variable(false)

  class LeftCellIcon(iconStyle: String, description: String, onClick: MouseEvent => Unit) extends RxElement {
    private val baseStyle = "width: 25px; "
    override def render: RxElement =
      span(
        cls -> "text-nowrap",
        isToolbarVisible.map {
          case true =>
            style -> s"${baseStyle}; visibility: visible;"
          case false =>
            style -> s"${baseStyle}; visibility: hidden;"
        },
        new EditorIcon(
          description,
          iconStyle,
          onClick = onClick
        )
      )
  }

  override def render: RxElement = {
    div(
      cls -> "w-100",
      table(
        id  -> cellIdStr,
        cls -> defaultStyle,
        onclick -> { e: MouseEvent =>
          notebookEditor.focusOnCell(thisCell)
        },
        onmouseover -> { e: MouseEvent =>
          isToolbarVisible := true
        },
        onmouseout -> { e: MouseEvent =>
          isToolbarVisible := false
        },
        // Query editor
        tr(
          cls -> "mt-1",
          td(
            cls -> "align-top text-center bg-light",
            new LeftCellIcon(
              "fa-plus",
              "Add a new cell",
              onClick = { e: MouseEvent =>
                val newCell = notebookEditor.insertCellAfter(thisCell)
                notebookEditor.focusOnCell(newCell)
              }
            )
          ),
          td(
            cls -> "align-middle",
            // This setting is necessary for placing cell menu icons at the right-top corner
            style -> "display: flex; flex-direction: column; position: relative; ",
            new NotebookCellToolbar(thisCell, isToolbarVisible),
            editor
          )
        ),
        // Query status
        tr(
          td(
            cls -> "align-top text-center bg-light",
            currentQueryInfo.filter(_.result.isDefined).map { qi =>
              new LeftCellIcon(
                "fa-caret-down",
                "Fold/Unfold",
                onClick = { e: MouseEvent =>
                  toggleResultView
                }
              )
            }
          ),
          td(
            style -> "min-height: 23px; ",
            Rx.join(currentQueryInfo, currentQueryId).map[RxElement] {
              case (None, Some(queryId)) =>
                div(
                  Rx.intervalMillis(800)
                    .flatMap { _ =>
                      notebookEditor.rpcRxClient.FrontendApi
                        .getQueryInfo(queryId)
                        .map {
                          case Some(qi) =>
                            if (qi.queryStatus.isFinished) {
                              currentQueryInfo := Some(qi)
                            }
                            new QueryStatusLine(qi)
                          case None =>
                            small("Query not found")
                        }
                    }.startWith(small("Loading ..."))
                )
              case (Some(qi), _) =>
                new QueryStatusLine(qi)
              case (None, _) =>
                span()
            }
          )
        ),
        // Query results
        tr(
          td(
          ),
          td(
            div(
              showResult.map {
                case true =>
                  cls -> "collapse show"
                case false =>
                  cls -> "collapse hide"
              },
              id -> s"${resultCellId}",
              currentQueryInfo.map { qi =>
                qi.result match {
                  case Some(qr) =>
                    new QueryResultViewer(qr)
                  case _ =>
                    span()
                }
              }
            )
          )
        )
      )
    )
  }
}

class NotebookCellToolbar(thisCell: NotebookCell, isToolbarVisible: RxVar[Boolean]) extends RxElement {

  private def cellMenuIcon(name: String, faStyle: String, onClick: MouseEvent => Unit) = {
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

  private def dropdownItem(iconStyle: String) = {
    val baseStyle = "dropdown-item px-3"
    a(
      cls -> "dropdown-item px-3",
      i(
        cls   -> s"fa ${iconStyle} mr-2",
        style -> "width: 11px;"
      ),
      onmouseover -> { e: MouseEvent =>
        e.getCurrentTarget.foreach { el =>
          el.className = s"${baseStyle} text-white bg-info"
        }
      },
      onmouseout -> { e: MouseEvent =>
        e.getCurrentTarget.foreach { el =>
          el.className = baseStyle
        }
      }
    )
  }

  private val cellMenuStyle =
    Seq("background: #ffffff", "display: flex", "position: absolute", "top: -20px", "right: 10px", "z-index: 1070")
      .mkString("; ")

  override def render: RxElement = {
    div(
      style -> "display: flex; flex-direction: column; position: relative; ",
      div(
        isToolbarVisible.map {
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
              thisCell.runCell
            }
          ),
          div(
            cls -> "btn-group",
            cellMenuIcon(
              "Copy",
              "fa-clipboard",
              onClick = { e: MouseEvent => }
            )(
              aria.haspopup  -> true,
              aria.expanded  -> false,
              data("toggle") -> "dropdown"
            ),
            span(
              cls -> "dropdown-menu",
              // Need to set a higher z-index than query result table header (1020)
              style -> "z-index: 1070; ",
              dropdownItem("fa-edit")(
                "Copy SQL",
                onclick -> { e: MouseEvent =>
                  val clipboardText = new StringBuilder()
                  clipboardText.append(s"${thisCell.getTextValue}")
                  Clipboard.writeText(clipboardText.result())
                }
              ),
              dropdownItem("fa-sticky-note")(
                "Copy SQL & Results",
                onclick -> { e: MouseEvent =>
                  val clipboardText = new StringBuilder()
                  clipboardText.append(s"${thisCell.getTextValue}\n\n")
                  thisCell.getQueryInfo.foreach { qi =>
                    qi.result.map { result =>
                      clipboardText.append(QueryResultPrinter.print(result))
                    }
                  }
                  Clipboard.writeText(clipboardText.result())
                }
              )
            )
          ),
          cellMenuIcon(
            name = "Move Up",
            "fa-angle-up",
            { e: MouseEvent =>
              thisCell.notebookEditor.moveUp(thisCell)
            }
          ),
          cellMenuIcon(
            "Move Down",
            "fa-angle-down",
            { e: MouseEvent =>
              thisCell.notebookEditor.moveDown(thisCell)
            }
          ),
          cellMenuIcon(
            "Delete",
            "fa-trash",
            { e: MouseEvent =>
              thisCell.notebookEditor.deleteCell(thisCell)
            }
          ),
          cellMenuIcon(
            "Fold/Unfold",
            "fa-caret-down",
            { e: MouseEvent =>
              thisCell.toggleResultView
            }
          ),
          div(
            cls -> "btn-group",
            cellMenuIcon(
              "Menu",
              "fa-ellipsis-v",
              onClick = { e: MouseEvent => }
            )(
              aria.haspopup  -> true,
              aria.expanded  -> false,
              data("toggle") -> "dropdown"
            ),
            span(
              cls -> "dropdown-menu",
              // Need to set a higher z-index than query result table header (1020)
              style -> "z-index: 1070; ",
              dropdownItem("fa-menu")(
                "More menu",
                onclick -> { e: MouseEvent =>
                  // TODO
                }
              )
            )
          )
        )
      )
    )
  }
}
