package wvlet.querybase.ui.component.page

import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.HTMLInputElement
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.FrontendApi.{SearchItem, SearchRequest, SearchResponse}
import wvlet.querybase.ui.component.{Navbar, findHTMLElement}
import wvlet.querybase.ui.component.common.{LabeledForm, VStack}
import wvlet.querybase.ui.component.notebook.NotebookEditor
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}
import wvlet.querybase.ui.{RPCQueue, RPCService}

import java.util.concurrent.TimeUnit

/**
  */
class ExplorePage(notebookEditor: NotebookEditor, serviceJSClient: ServiceJSClient) extends RxElement with RPCQueue {

  private val searchResults = Rx.variable(SearchResponse(Seq.empty))
  private val searchForm = SearchForm(
    onChangeHandler = { keyword: String =>
      serviceJSClient.FrontendApi
        .search(SearchRequest(keyword = keyword)).foreach { resp =>
          searchResults := resp
        }
    }
  )

  private val elementId = ULID.newULIDString

  override def render: RxElement = {
    // TODO Support onDidMount in RxElement
    scalajs.js.timers.setTimeout(10) {
      searchForm.focus
    }

    div(
      id    -> elementId,
      style -> s"height: calc(100vh - ${Navbar.navbarHeight}px); overflow-y: auto;",
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
  override def render: RxElement = div(
    cls -> "dropdown",
    div(
      cls -> "dropdown-menu show",
      list.map { x =>
        a(
          cls -> "dropdown-item",
          x.title
        )
      }
    ).unless(list.isEmpty)
  )
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
