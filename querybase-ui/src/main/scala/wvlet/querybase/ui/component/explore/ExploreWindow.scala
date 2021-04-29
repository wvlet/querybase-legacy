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
import wvlet.querybase.ui.component.{ShortcutKeyDef, ShortcutKeys}
import wvlet.querybase.ui.component.common.{LabeledForm, VStack}
import wvlet.querybase.ui.component.notebook.NotebookEditor

import scala.collection.immutable.ListMap

/**
  */
class ExploreWindow(notebookEditor: NotebookEditor, serviceJSClient: ServiceJSClient) extends RxElement with RPCQueue {

  private val searchResults = Rx.variable(SearchResponse(Seq.empty))
  private val searchForm = SearchForm(
    onChangeHandler = search
  )

  private def search(keyword: String): Unit = {
    serviceJSClient.FrontendApi
      .search(SearchRequest(keyword = keyword)).foreach { resp =>
        searchResults := resp
      }
  }

  // Do the init search
  search("")

  private def shortcutKeys = new ShortcutKeys(
    Seq(
      ShortcutKeyDef(
        keyCode = 191,
        description = "Enter search box",
        handler = { e: Event =>
          searchForm.focus
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
        searchForm,
        searchResults.map { x =>
          SearchCandidates(x.results)
        }
      )
    )
  }
}

case class SearchForm(onChangeHandler: String => Unit = { e: String => }) extends RxElement with LogSupport {
  val elementId = ULID.newULID.toString()

  def onChange(f: String => Unit): SearchForm = this.copy(onChangeHandler = f)

  def focus: Unit = {
    form.focus
  }

  private val form =
    LabeledForm()
      .withLabel(i(cls -> "fa fa-search"))
      .withPlaceholder("Search ...")
      .onChange { keyword: String => onChangeHandler(keyword) }

  override def render: RxElement = {
    form
  }
}

case class SearchCandidates(list: Seq[SearchItem]) extends RxElement {
  private def iconStyle(kind: String): String = kind match {
    case "service"  => "fa fa-project-diagram"
    case "table"    => "fa fa-table"
    case "query"    => "fa fa-stream"
    case "notebook" => "fa fa-book-open"
    case _          => "fa fa-search"
  }

  override def render: RxElement = {
    div(
      cls -> "container-fluid px-0",
      for ((itemType, items) <- list.groupBy(_.kind) if items.nonEmpty) yield {
        VStack(
          h6(cls -> "dropdown-header", itemType.capitalize),
          items.map { x =>
            a(
              cls -> "dropdown-item text-secondary ml-2",
              i(cls -> iconStyle(x.kind)),
              span(
                cls -> "ml-2",
                x.title
              )
            )
          }
        )
      }
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
