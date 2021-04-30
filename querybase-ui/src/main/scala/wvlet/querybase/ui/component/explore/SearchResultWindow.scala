package wvlet.querybase.ui.component.explore

import org.scalajs.dom.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.SearchApi.SearchItem
import wvlet.querybase.ui.component.DO_NOTHING
import wvlet.querybase.ui.component.common.VStack

/**
  */
case class SearchResultWindow(private val onSelectHandler: SearchItem => Unit = DO_NOTHING)
    extends RxElement
    with LogSupport {
  private val show  = Rx.variable(false)
  private val items = Rx.variable(Seq.empty[SearchItem])

  def onSelect(f: SearchItem => Unit) = this.copy(
    onSelectHandler = f
  )

  def setList(newList: Seq[SearchItem]): Unit = {
    items := newList
    show := newList.nonEmpty
  }

  def hide: Unit = {
    show := false
  }

  private def iconStyle(kind: String): String = kind match {
    case "service"  => "fa fa-project-diagram"
    case "table"    => "fa fa-table"
    case "database" => "fa fa-database"
    case "query"    => "fa fa-stream"
    case "notebook" => "fa fa-book-open"
    case _          => "fa fa-search"
  }

  override def render: RxElement = {
    div(
      cls -> "dropdown px-0",
      div(
        show.map {
          case true =>
            cls -> "dropdown-menu mt-0 show"
          case false =>
            cls -> "dropdown-menu"
        },
        items.map { list =>
          for ((itemType, items) <- list.groupBy(_.kind) if items.nonEmpty) yield {
            VStack(
              h6(cls -> "dropdown-header", itemType.capitalize),
              items.map { x =>
                a(
                  cls -> "dropdown-item text-secondary ml-2",
                  onclick -> { e: MouseEvent =>
                    onSelectHandler(x)
                    show := false
                  },
                  i(cls -> iconStyle(x.kind)),
                  span(
                    cls -> "ml-2",
                    x.title
                  )
                )
              }
            )
          }
        }
      )
    )
  }
}
