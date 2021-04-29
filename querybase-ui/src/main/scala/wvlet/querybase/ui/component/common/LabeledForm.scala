package wvlet.querybase.ui.component.common

import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.HTMLInputElement
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.findHTMLElement

/**
  */
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
    dom.document.getElementById(formId) match {
      case el: HTMLInputElement =>
        el.focus()
        el.select()
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
      cls         -> "form-control",
      tpe         -> "text",
      placeholder -> placeholderText,
      aria.label  -> "search input",
      onkeyup     -> { e: KeyboardEvent => onChangeHandler(getText) }
    )
  )
}
