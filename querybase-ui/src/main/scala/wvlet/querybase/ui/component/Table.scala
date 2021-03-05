package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.all.{cls, small, style, table, tbody, th, thead, tr}
import wvlet.airframe.rx.html.{RxComponent, RxElement}

class Table(columnNames: Seq[String])(body: RxElement*) extends RxElement {
  override def render: RxElement = {
    table(
      cls   -> "table table-sm table-hover",
      style -> "overflow: auto; border-collapse: collapse; height: 100px;",
      thead(
        cls -> "thead-light",
        tr(
          //cls -> "text-center",
          columnNames.map { columnName =>
            th(
              cls   -> "font-weight-normal",
              style -> "position: sticky; top 0;",
              columnName
            )
          }
        )
      ),
      tbody(
        body
      )
    )
  }
}
