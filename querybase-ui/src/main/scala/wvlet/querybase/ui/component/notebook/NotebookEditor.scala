package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{document, window}
import org.scalajs.dom.raw.{HTMLElement, KeyboardEvent, MouseEvent}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOptionVar, RxVar}
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.FrontendApi._
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.ui.component._
import wvlet.querybase.ui.component.common.Clipboard
import wvlet.querybase.ui.component.editor.TextEditor

import scala.concurrent.Future

/**
  */
class NotebookEditor(
    serviceSelector: ServiceSelector,
    private[ui] val rpcRxClient: ServiceJSClientRx,
    private[ui] val rpcClient: ServiceJSClient
) extends RxElement
    with LogSupport {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private var cells: Seq[NotebookCell] = Seq(
    new NotebookCell(this, ULID.newULID, NotebookCellData("select 1", None), focused = true)
  )
  private val session = NotebookSession("default")

  // Trigger this variable when the cells are updated
  private val updated    = Rx.variable(false)
  private val schemaForm = new SchemaForm()

  private val notebookSaver = new NotebookSaver(this, rpcClient)

  private var hasLoaded: Boolean = false

  override def beforeRender: Unit = {
    if (!hasLoaded) {
      val future = rpcClient.FrontendApi
        .getNotebook(GetNotebookRequest(session))
        .filter(_.isDefined)
        .map { data =>
          info(s"Read the session data from the server")
          cells = data.get.cells.map { cellData =>
            new NotebookCell(this, ULID.newULID, cellData, focused = false)
          }
          updated.forceSet(true)
        }

      future.onComplete { case _ =>
        hasLoaded = true
        // Start NotebookSaver after the reading the default session finished
        notebookSaver.start
      }
    } else {
      notebookSaver.start
    }
  }

  override def beforeUnmount: Unit = {
    notebookSaver.save(getNotebookData)
    notebookSaver.stop
  }

  def currentSession: NotebookSession = session

  def getNotebookData: NotebookData = {
    val cellData = cells.map { c =>
      NotebookCellData(
        text = c.getTextValue,
        queryInfo = c.getQueryInfo
      )
    }
    NotebookData(cellData)
  }

  private val shortcutKeys = new ShortcutKeys(
    Seq(
      ShortcutKeyDef(
        keyCode = KeyCode.Escape,
        ctrl = false,
        meta = false,
        description = "Switch mode",
        handler = { e: KeyboardEvent =>
          info(s"Switch editor mode")
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.P,
        ctrl = true,
        shift = true,
        description = "Add a new cell above",
        handler = { e: KeyboardEvent =>
          info(s"Add a new cell above")
          cells.find(_.hasFocus).foreach { cell =>
            val inserted = insertCellBefore(cell)
            focusOnCell(inserted)
          }
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.N,
        ctrl = true,
        shift = true,
        description = "Add a new cell below",
        handler = { e: KeyboardEvent =>
          info(s"Add a new cell below")
          cells.find(_.hasFocus).foreach { cell =>
            val inserted = insertCellAfter(cell)
            focusOnCell(inserted)
          }
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.P,
        ctrl = true,
        meta = true,
        description = "Move to the upper cell",
        handler = { e: KeyboardEvent =>
          info(s"Move to the upper cell")
          cells.find(_.hasFocus).foreach { cell =>
            getCellIndex(cell).foreach { cellIndex =>
              getCell(cellIndex - 1).foreach {
                focusOnCell(_)
              }
            }
          }
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.N,
        ctrl = true,
        meta = true,
        description = "Move to the lower cell",
        handler = { e: KeyboardEvent =>
          info(s"Move to the lower cell")
          cells.find(_.hasFocus).foreach { cell =>
            getCellIndex(cell).foreach { cellIndex =>
              getCell(cellIndex + 1).foreach {
                focusOnCell(_)
              }
            }
          }
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.D,
        ctrl = true,
        meta = true,
        description = "Delete cell",
        handler = { e: KeyboardEvent =>
          info(s"Delete cell")
          cells.find(_.hasFocus).foreach { cell =>
            deleteCell(cell)
          }
        }
      )
    )
  )
  private val notebookId = s"notebook-${ULID.newULID}"

  override def render: RxElement = {
    // TODO: Add afterRender event hook support to airframe-rx-html
    scala.scalajs.js.timers.setTimeout(100) {
      cells.headOption.foreach {
        focusOnCell(_)
      }
    }
    div(
      id -> notebookId,
      shortcutKeys,
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

        document.getElementById(notebookId) match {
          case e: HTMLElement =>
            Option(e.parentElement).foreach { parent =>
              val windowTop    = parent.scrollTop
              val windowHeight = parent.clientHeight
              val frameHeight  = c.editorHeight
              val frameTop     = c.offsetTop
              val frameOffset  = 90

              trace(s"window top: ${window}, height: ${windowHeight}, height:${frameHeight}, frameTop:${frameTop}")
              if (windowTop >= frameTop - frameOffset) {
                parent.scrollTop = frameTop - frameOffset
              }
              if (windowTop + windowHeight < frameTop + frameHeight - frameOffset) {
                parent.scrollTop = frameTop + frameHeight - windowHeight + frameOffset
              }
            }
          case _ =>
        }

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
    new NotebookCell(this, ULID.newULID, NotebookCellData("", None))
  }

  def insertCellAfter(cell: NotebookCell): NotebookCell = {
    val targetCellIndex = getCellIndex(cell)
    val ci              = targetCellIndex.map(_ + 1).getOrElse(cells.size).min(cells.size)
    insertCellAt(ci)
  }

  def insertCellBefore(cell: NotebookCell): NotebookCell = {
    val baseCellIndex = getCellIndex(cell)
    val ci            = baseCellIndex.map(_ - 1).getOrElse(0).max(0)
    insertCellAt(ci)
  }

  private def insertCellAt(ci: Int): NotebookCell = {
    val nc       = newCell
    val newCells = Seq.newBuilder[NotebookCell]
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

class NotebookCell(
    val notebookEditor: NotebookEditor,
    cellId: ULID,
    initData: NotebookCellData,
    focused: Boolean = false
) extends RxElement
    with LogSupport {
  thisCell =>

  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private val currentQueryId                           = Rx.optionVariable[String](initData.queryInfo.map(_.queryId))
  private val currentQueryInfo: RxOptionVar[QueryInfo] = Rx.optionVariable(initData.queryInfo)

  def runCell: Unit = {
    notebookEditor.submitQuery(editor.getTextValue).foreach { queryId =>
      currentQueryId := Some(queryId)
      showResult := true
      currentQueryInfo := None
    }
  }

  private val editor = new TextEditor(
    initialValue = initData.text,
    onEnter = { text: String =>
      if (text.trim.nonEmpty) {
        runCell
        notebookEditor.focusOnCell(thisCell)
      }
    },
    onExitUp = { () =>
      notebookEditor.getCellIndex(thisCell).foreach { cellIndex =>
        notebookEditor.getCell(cellIndex - 1).foreach { cell =>
          notebookEditor.focusOnCell(cell)
        }
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

  private val frameId      = s"frame-${cellId}"
  private val editorId     = s"cell-${cellId}"
  private val resultCellId = s"result-${cellId}"

  private val defaultStyle = "w-100 shadow-none border border-white"
  private val focusedStyle = "w-100 shadow-sm border border-info"

  private var _hasFocus = false
  def hasFocus: Boolean = _hasFocus

  def unfocus: Unit = {
    findHTMLElement(frameId).foreach {
      _.className = defaultStyle
    }
    _hasFocus = false
  }
  def focus: Unit = {
    if (!_hasFocus) {
      editor.focus
      findHTMLElement(frameId).foreach { el =>
        el.className = focusedStyle
      }

    }
    _hasFocus = true
  }

  private def getFrame: Option[HTMLElement] = {
    document.getElementById(frameId) match {
      case el: HTMLElement =>
        Some(el)
      case _ =>
        None
    }
  }

  def frameHeight: Int = {
    getFrame.map(_.scrollHeight).getOrElse(50)
  }
  def editorHeight: Int = {
    document.getElementById(editorId) match {
      case e: HTMLElement =>
        e.scrollHeight
      case _ => 18
    }
  }
  def offsetTop: Int = {
    getFrame.map(_.offsetTop.toInt).getOrElse(0)
  }

  def formatCode: Unit = editor.formatCode

  def getEditor: TextEditor = editor

  def getTextValue: String = {
    editor.getTextValue
  }
  def setTextValue(text: String): Unit = {
    editor.setTextValue(text)
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
      id  -> frameId,
      table(
        id  -> editorId,
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
                val newCell = notebookEditor.insertCellBefore(thisCell)
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
        )
      ),
      table(
        style -> "width: auto;",
        // Query results
        tr(
          td(
            style -> "width: 25px;"
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
