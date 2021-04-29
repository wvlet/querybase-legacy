package wvlet.querybase.ui.component.page

import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.HTMLInputElement
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.Navbar
import wvlet.querybase.ui.component.common.VStack
import wvlet.querybase.ui.component.notebook.NotebookEditor

/**
  */
class ExplorePage(notebookEditor: NotebookEditor) extends RxElement {

  private val searchForm = new SearchForm()

  override def render: RxElement = {
    div(
      style -> s"height: calc(100vh - ${Navbar.navbarHeight}px); overflow-y: auto;",
      VStack(
        searchForm
      )
    )
  }
}

class SearchForm extends RxElement with LogSupport {
  val elementId = ULID.newULID.toString()

  private val form =
    LabeledForm()
      .withLabel(i(cls -> "fa fa-search"))
      .withPlaceholder("Search ...")
      .onChange { keyword: String =>
        info(s"Search: ${keyword}")
      }

  override def render: RxElement = {
    form
  }
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
      id          -> formId,
      cls         -> "form-control mr-2",
      tpe         -> "text",
      placeholder -> placeholderText,
      aria.label  -> "search input",
      onkeyup     -> { e: KeyboardEvent => onChangeHandler(getText) }
    )
  )
}
