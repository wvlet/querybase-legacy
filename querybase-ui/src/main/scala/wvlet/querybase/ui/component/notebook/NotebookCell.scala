package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOptionVar}
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.FrontendApi.NotebookCellData
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.querybase.ui.component.findHTMLElement

/** */
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
      currentQueryId   := Some(queryId)
      showResult       := true
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
