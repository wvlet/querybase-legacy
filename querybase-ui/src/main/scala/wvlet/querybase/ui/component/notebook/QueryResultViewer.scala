package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryResult

/**
  */
class QueryResultViewer(r: QueryResult) extends RxElement {
  override def render: RxElement = {
    val columnNames: Seq[String] = r.schema.map(_.name)
    div(
      style -> "overflow-x: scroll; width: calc(100vw - 270px); ",
      table(
        cls   -> "table table-sm table-bordered",
        style -> "font-size: 12px;",
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
                  cls   -> "text-truncate",
                  style -> "max-width: 150px;",
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
