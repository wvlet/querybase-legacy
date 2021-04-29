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
import wvlet.querybase.ui.component.common.VStack
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
          VStack(
            x.results.map { item =>
              SearchItemCard(item)
            }
          )
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

case class LabeledForm(
    elementId: String = ULID.newULIDString,
    formId: String = ULID.newULIDString,
    labelElement: RxElement = span(),
    placeholderText: String = "input...",
    onChangeHandler: String => Unit = { e: String => }
) extends RxElement
    with LogSupport {

  def withLabel(newLabel: RxElement): LabeledForm   = this.copy(labelElement = newLabel)
  def withPlaceholder(newText: String): LabeledForm = this.copy(placeholderText = newText)
  def onChange(f: String => Unit): LabeledForm      = this.copy(onChangeHandler = f)

  def getText: String = {
    dom.document.getElementById(formId) match {
      case e: HTMLInputElement =>
        e.value
      case _ =>
        ""
    }
  }

  def focus: Unit = {
    findHTMLElement(formId).foreach { el =>
      el.focus()
    }
  }

  override def render: RxElement = div(
    id  -> elementId,
    cls -> "input-group m-1",
    div(
      cls -> "input-group-prepend",
      label(
        cls -> "input-group-text",
        labelElement
      )
    ),
    input(
      id -> formId,
      autofocus,
      cls         -> "form-control mr-2",
      tpe         -> "text",
      placeholder -> placeholderText,
      aria.label  -> "search input",
      onkeyup     -> { e: KeyboardEvent => onChangeHandler(getText) }
    )
  )
}
