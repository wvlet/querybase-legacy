package wvlet.querybase.ui.component.explore

import org.scalajs.dom.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.SearchApi.SearchItem
import wvlet.querybase.ui.component.DO_NOTHING
import wvlet.querybase.ui.component.common.{MouseOverToggle, VStack}

/**
  */
case class SearchResultWindow(private val onSelectHandler: SearchItem => Unit = DO_NOTHING)
    extends RxElement
    with LogSupport {
  private val show  = Rx.variable(false)
  private val focus = Rx.variable(false)
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

  def hasFocus: Boolean = {
    focus.get
  }

  override def render: RxElement = {
    div(
      id  -> "search-result-window",
      cls -> "dropdown px-0",
      div(
        show.map {
          case true =>
            cls -> "dropdown-menu mt-0 show"
          case false =>
            cls -> "dropdown-menu"
        },
        MouseOverToggle(focus),
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
                  ItemIcon(x.kind),
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
