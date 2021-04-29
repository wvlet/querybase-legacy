package wvlet.querybase.ui.component.explore

import org.scalajs.dom.{Event, MouseEvent}
import org.scalajs.dom.ext.KeyCode
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.FrontendApi.{SearchItem, SearchRequest, SearchResponse}
import wvlet.querybase.api.frontend.ServiceJSClient
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.{DO_NOTHING, ShortcutKeyDef, ShortcutKeys}
import wvlet.querybase.ui.component.common.{LabeledForm, VStack}
import wvlet.querybase.ui.component.notebook.NotebookEditor

import scala.collection.immutable.ListMap

/**
  */
class ExploreWindow(notebookEditor: NotebookEditor, serviceJSClient: ServiceJSClient)
    extends RxElement
    with RPCQueue
    with LogSupport {

  private val searchForm = LabeledForm()
    .withLabel(i(cls -> "fa fa-search"))
    .withPlaceholder("Search ...")
    .onChange { keyword: String => search(keyword) }
    .onEnter { keyword: String =>
      info(s"Start search: ${keyword}")
    }

  private val searchResultList = SearchCandidates().onSelect { x: SearchItem =>
    info(s"Search :${x}")
  }

  private def search(keyword: String): Unit = {
    serviceJSClient.FrontendApi
      .search(SearchRequest(keyword = keyword)).foreach { resp =>
        searchResultList.setList(resp.results)
      }
  }

  private def exitSearch: Unit = {
    searchResultList.hide
    searchForm.blur
  }

  private def shortcutKeys = new ShortcutKeys(
    Seq(
      ShortcutKeyDef(
        keyCode = 191,
        description = "Enter search box",
        handler = { e: Event =>
          searchForm.focus
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.Escape,
        description = "Exit from search",
        handler = { e: Event =>
          exitSearch
        }
      )
    )
  )

  override def render: RxElement = {
    // TODO Support onDidMount in RxElement
    scalajs.js.timers.setTimeout(10) {
      searchForm.focus
    }

    div(
      shortcutKeys,
      VStack(
        div(
          style -> "width: 500px;",
          searchForm
        ),
        searchResultList
      )
    )
  }
}

case class SearchCandidates(private val onSelectHandler: SearchItem => Unit = DO_NOTHING)
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

case class SearchItemCard(result: SearchItem) extends RxElement {
  override def render: RxElement = div(
    cls   -> "card",
    style -> "width: 600px",
    div(
      cls -> "card-body",
      h6(
        cls -> "card-tittle",
        result.title
      ),
      div(
        cls -> "card-text",
        "SQL ..."
      )
    )
  )
}
