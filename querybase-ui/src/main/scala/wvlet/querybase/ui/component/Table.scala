package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.all.{cls, table, tbody, th, thead, tr}
import wvlet.airframe.rx.html.{RxComponent, RxElement}

class Table(columnNames: Seq[String]) extends RxElement {
  override def render: RxElement = {
    table(
      cls -> "table table-sm table-hover",
      thead(
        cls -> "thead-light",
        tr(
          //cls -> "text-center",
          columnNames.map { columnName => th(columnName) }
        )
      )
    )
  }
}
