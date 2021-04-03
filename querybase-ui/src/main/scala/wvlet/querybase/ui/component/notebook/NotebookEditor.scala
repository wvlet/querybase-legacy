package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.{RxElement, tag}
import wvlet.airframe.rx.html.all._
import wvlet.airframe.rx.{Rx, RxOption, RxOptionVar, RxStream}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{QueryInfo, QueryResult}
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.api.frontend.FrontendApi.SubmitQueryRequest
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.api.frontend.code.NotebookApi.Cell
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component._
import wvlet.querybase.ui.component.common.Table
import wvlet.querybase.ui.component.editor.TextEditor

import scala.concurrent.Future

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
        source = "select * from sample_datasets.www_access limit 10",
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
      focusOnCell((index + 1).max(0), create = true)
    }

    private val editor = new TextEditor(
      cell.source,
      onEnter = { text: String =>
        if (text.trim.nonEmpty) {
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

    private val cellId = s"cell-${index}"

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

    override def render: RxElement = {
      div(
        cls   -> "w-100",
        style -> "overflow-x: scroll;",
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
              new PlayIcon(onClick = { e: MouseEvent => run })
            ),
            td(
              cls -> "align-bottom",
              editor
            )
          ),
          tr(
            td(),
            td(
              div(
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
                        }.startWith(small("Loading"))
                    )
                  case (None, None) =>
                    span("> ")
                }
              )
            )
          )
        )
      )
    }
  }
}

class QueryStatusLine(queryInfo: Option[QueryInfo]) extends RxElement with LogSupport {

  private def status(s: QueryStatus): RxElement = {
    QueryListPanel.renderStatus(s)(cls += "mr-2")
  }

  private def queryStatusLine(qi: QueryInfo): RxElement = {
    small(
      status(qi.queryStatus),
      span(
        s"[${qi.serviceType}:${qi.serviceName}] ${qi.queryId}: ${qi.elapsed}"
      )
    )
  }

  private def renderQueryResult(r: QueryResult): RxElement = {
    val columnNames: Seq[String] = r.schema.map(_.name)
    info(r)
    div(
      style -> "overflow-x: scroll;",
      table(
        cls   -> "table table-sm",
        style -> "font-size: 11px;",
        thead(
          cls -> "thead-light",
          tr(
            columnNames.map { x =>
              th(cls -> "font-weight-normal text-truncate", x)
            }
          )
        ),
        tbody(
          r.rows.map { row =>
            tr(
              row.map { col =>
                val v: String = Option(col).map(_.toString).getOrElse("")
                td(
                  cls -> "text-truncate",
                  //style -> "max-width: 80px;",
                  title -> v,
                  v
                )
              }
            )
          }
        )
      )
    )
  }

  override def render: RxElement = queryInfo match {
    case Some(qi) if qi.result.nonEmpty =>
      div(
        queryStatusLine(qi),
        renderQueryResult(qi.result.get)
      )
    case Some(qi) =>
      queryStatusLine(qi)
    case None =>
      small(
        status(QueryStatus.STARTING),
        "running..."
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
          el.className = s"${baseCls} text-primary"
        }
      },
      onmouseout -> { e: MouseEvent =>
        e.getCurrentTarget.foreach {
          _.className = s"${baseCls} text-secondary"
        }
      }
    )

}
