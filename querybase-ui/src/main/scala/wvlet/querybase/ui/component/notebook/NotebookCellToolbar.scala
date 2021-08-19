package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.RxVar
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.common.Clipboard
import wvlet.querybase.ui.component._

/** */
class NotebookCellToolbar(thisCell: NotebookCell, isToolbarVisible: RxVar[Boolean]) extends RxElement {
  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

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
    Seq("background: #ffffff", "display: flex", "position: absolute", "top: -16px", "right: 10px", "z-index: 1070")
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
              ),
              dropdownItem("fa-sticky-note")(
                "Copy Results",
                onclick -> { e: MouseEvent =>
                  val clipboardText = new StringBuilder()
                  thisCell.getQueryInfo.foreach { qi =>
                    qi.result.map { result =>
                      clipboardText.append(QueryResultPrinter.toTSV(result))
                    }
                  }
                  Clipboard.writeText(clipboardText.result())
                }
              )
            )
          ),
          // Undo doens't work well
//          cellMenuIcon(
//            name = "Undo",
//            "fa-undo",
//            { e: MouseEvent =>
//              thisCell.getEditor.undo
//            }
//          ),
          cellMenuIcon(
            name = "Format Query",
            "fa-indent",
            { e: MouseEvent =>
              //thisCell.formatCode
              thisCell.notebookEditor.rpcClient.FrontendApi.formatQuery(thisCell.getTextValue).foreach { formatted =>
                thisCell.setTextValue(formatted)
              }
            }
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
