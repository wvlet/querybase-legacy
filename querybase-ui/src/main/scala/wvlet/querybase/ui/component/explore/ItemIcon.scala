package wvlet.querybase.ui.component.explore

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, i}

/**
  */
case class ItemIcon(kind: String) extends RxElement {
  private def iconStyle(kind: String): String = kind match {
    case "service"  => "fa fa-project-diagram"
    case "table"    => "fa fa-table"
    case "database" => "fa fa-database"
    case "query"    => "fa fa-stream"
    case "notebook" => "fa fa-book-open"
    case _          => "fa fa-search"
  }

  override def render: RxElement = i(cls -> iconStyle(kind))

}
