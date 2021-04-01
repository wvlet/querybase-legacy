package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

class Table(columnNames: Seq[String])(body: RxElement*) extends RxElement {
  override def render: RxElement = {
    table(
      cls -> "table table-sm table-hover w-100",
      //style -> "overflow: auto; border-collapse: collapse; height: 100px;",
      // TODO: sticky header https://stackoverflow.com/questions/47723996/table-with-fixed-thead-and-scrollable-tbody
      thead(
        cls -> "thead-light",
        tr(
          //cls -> "text-center",
          columnNames.map { columnName =>
            th(
              cls -> "font-weight-normal",
              //style -> "position: sticky; top 0;",
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
