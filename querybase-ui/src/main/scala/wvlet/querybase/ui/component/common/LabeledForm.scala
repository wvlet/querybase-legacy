package wvlet.querybase.ui.component.common

import org.scalajs.dom
import org.scalajs.dom.{Event, KeyboardEvent}
import org.scalajs.dom.raw.HTMLInputElement
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport

/**
  */
case class LabeledForm(
    elementId: String = ULID.newULIDString,
    formId: String = ULID.newULIDString,
    labelElement: RxElement = span(),
    placeholderText: String = "input...",
    onChangeHandler: String => Unit = { e: String => },
    onBlurHandler: () => Unit = { () => }
) extends RxElement
    with LogSupport {

  def withLabel(newLabel: RxElement): LabeledForm   = this.copy(labelElement = newLabel)
  def withPlaceholder(newText: String): LabeledForm = this.copy(placeholderText = newText)
  def onChange(f: String => Unit): LabeledForm      = this.copy(onChangeHandler = f)
  def onBlur(f: () => Unit): LabeledForm            = this.copy(onBlurHandler = f)

  private def getFormInputElement: Option[HTMLInputElement] = {
    dom.document.getElementById(formId) match {
      case e: HTMLInputElement => Some(e)
      case _                   => None
    }
  }

  def getText: String = getFormInputElement.map(_.value).getOrElse("")

  def focus: Unit = getFormInputElement.foreach { el =>
    el.focus()
    el.select()
  }

  def blur: Unit = getFormInputElement.foreach { el =>
    el.blur()
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
      cls         -> "form-control",
      tpe         -> "text",
      placeholder -> placeholderText,
      aria.label  -> "search input",
      onkeyup     -> { e: KeyboardEvent => onChangeHandler(getText) },
      onblur      -> { e: Event => onBlurHandler() }
    )
  )
}
