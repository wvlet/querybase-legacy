package wvlet.querybase.ui.component.explore

import org.scalajs.dom.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.FrontendApi.{SearchItem, SearchRequest, SearchResponse}
import wvlet.querybase.api.frontend.ServiceJSClient
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.common.{LabeledForm, VStack}
import wvlet.querybase.ui.component.notebook.NotebookEditor

/**
  */
class ExploreWindow(notebookEditor: NotebookEditor, serviceJSClient: ServiceJSClient) extends RxElement with RPCQueue {

  private val searchResults = Rx.variable(SearchResponse(Seq.empty))
  private val searchForm = SearchForm(
    onChangeHandler = { keyword: String =>
      serviceJSClient.FrontendApi
        .search(SearchRequest(keyword = keyword)).foreach { resp =>
          searchResults := resp
        }
    }
  )

  override def render: RxElement = {
    // TODO Support onDidMount in RxElement
    scalajs.js.timers.setTimeout(10) {
      searchForm.focus
    }

    div(
      VStack(
        searchForm,
        searchResults.map { x =>
          SearchCandidates(x.results)
        },
        table(
          tr(
            td("service")
          ),
          tr(
            td("queries")
          )
        )
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

  private val show = Rx.variable(true)

  override def render: RxElement = div(
    cls -> "dropdown",
    div(
      show.map {
        case true =>
          cls -> "dropdown-menu show"
        case false =>
          cls -> "dropdown-menu"
      },
      list.map { x =>
        a(
          cls -> "dropdown-item",
          span(
            cls -> "text-black-50",
            x.kind match {
              case "service" =>
                i(cls -> "fa fa-project-diagram")
              case "table" =>
                i(cls -> "fa fa-table")
              case "query" =>
                i(cls -> "fa fa-stream")
              case "note-book" =>
                i(cls -> "fa fa-book-open")
              case _ =>
                i(cls -> "fa fa-book")
            }
          ),
          onclick -> { e: MouseEvent =>
            show := false
          },
          span(
            cls -> "ml-2",
            x.title
          )
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
