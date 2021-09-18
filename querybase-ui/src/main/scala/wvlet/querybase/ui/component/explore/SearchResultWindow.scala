package wvlet.querybase.ui.component.explore

import org.scalajs.dom.{Event, MouseEvent, document}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.HTMLElement
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.SearchApi.SearchItem
import wvlet.querybase.ui.component.{DO_NOTHING, ShortcutKeyDef, ShortcutKeys}
import wvlet.querybase.ui.component.common.{MouseOverToggle, VStack}

/** */
case class SearchResultWindow(private val onSelectHandler: SearchItem => Unit = DO_NOTHING)
    extends RxElement
    with LogSupport {
  private val show   = Rx.variable(false)
  private val _focus = Rx.variable(false)
  private val items  = Rx.variable(Seq.empty[SearchItem])

  private val selectedIndex = Rx.variable(0)

  def onSelect(f: SearchItem => Unit) = this.copy(
    onSelectHandler = f
  )

  def setList(newList: Seq[SearchItem]): Unit = {
    val updatedList = (for ((itemType, items) <- newList.groupBy(_.kind) if items.nonEmpty) yield {
      items
    }).flatten.toSeq
    items := updatedList
    show  := updatedList.nonEmpty
  }

  def hide: Unit = {
    show := false
  }

  def hasFocus: Boolean = {
    _focus.get
  }

  def select(index:Int): Unit = {
    selectedIndex := index
  }

  def up: Int = {
    selectedIndex.update(x => (x - 1).max(0))
    selectedIndex.get
  }
  def down: Int = {
    selectedIndex.update(x => (x + 1).min(items.get.size - 1))
    selectedIndex.get
  }

  def getSelected: Option[SearchItem] = {
    val lst = items.get
    val index = selectedIndex.get
    if(index >= 0 && index < lst.length) {
      Some(lst(index))
    }
    else {
      None
    }
  }

  override def render: RxElement = {
    def searchResults(visible: Boolean): RxElement = {
      div(
        cls -> "dropdown-menu",
        (cls += "mt-0 show").when(visible),
        MouseOverToggle(_focus),
        items.map { list =>
          VStack(
            list.zipWithIndex.map { case (x, index) =>
              //h6(cls -> "dropdown-header", itemType.capitalize),
              selectedIndex.map { i =>
                a(
                  cls -> "dropdown-item ml-2",
                  if (i != index) {
                    cls += "text-secondary"
                  } else {
                    cls += "text-light bg-primary"
                  },
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
            }
          )
        }
      )
    }

    div(
      id    -> "search-result-window",
      cls   -> "dropdown px-0",
      style -> "width: 561px;",
      show.map(x => searchResults(x))
    )
  }
}
