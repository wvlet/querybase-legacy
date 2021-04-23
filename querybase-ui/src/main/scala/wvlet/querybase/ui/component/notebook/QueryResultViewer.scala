package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryResult
import wvlet.querybase.ui.component.Sidebar

/**
  */
class QueryResultViewer(r: QueryResult) extends RxElement {
  override def render: RxElement = {
    val columnNames: Seq[String] = r.schema.map(_.name)
    div(
      style -> Seq(
        "overflow-x: scroll",
        "max-height: 300px",
        // max-width must be set to properly enable horizontal scroll (scroll-x)
        s"max-width: calc(100vw - ${Sidebar.sidebarWidth + 55}px)"
      ).mkString("; "),
      table(
        cls -> "table table-sm table-bordered",
        style -> Seq(
          "width: auto",
          "font-size: 12px"
        ).mkString("; "),
        thead(
          cls -> "thead-light",
          tr(
            columnNames.map { x =>
              th(
                // Fix table header to the top
                cls -> "sticky-top font-weight-normal text-truncate",
                // A workaround for hide the overflow at the table head
                style -> "top: -1px;",
                x
              )
            }
          )
        ),
        tbody(
          // Enable vertical scroll for long rows
          style -> "overflow-y: scroll; ",
          r.rows.map { row =>
            tr(
              row.map { col =>
                val v: String = Option(col).map(_.toString).getOrElse("")
                td(
                  cls   -> "text-truncate text-right",
                  style -> "max-width: 250px;",
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
}
