package wvlet.querybase.ui.component.common

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Event, KeyboardEvent}
import org.scalajs.dom.raw.HTMLInputElement
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.DO_NOTHING

/**
  */
case class LabeledForm(
    elementId: String = ULID.newULIDString,
    formId: String = ULID.newULIDString,
    labelElement: RxElement = span(),
    placeholderText: String = "input...",
    isSmall: Boolean = false,
    onEnterHandler: String => Unit = DO_NOTHING,
    onChangeHandler: String => Unit = DO_NOTHING,
    onBlurHandler: () => Unit = { () => }
) extends RxElement
    with LogSupport {

  def withLabel(newLabel: RxElement): LabeledForm   = this.copy(labelElement = newLabel)
  def withPlaceholder(newText: String): LabeledForm = this.copy(placeholderText = newText)
  def withSmallSize: LabeledForm                    = this.copy(isSmall = true)
  def onEnter(f: String => Unit): LabeledForm       = this.copy(onEnterHandler = f)
  def onChange(f: String => Unit): LabeledForm      = this.copy(onChangeHandler = f)
  def onBlur(f: () => Unit): LabeledForm            = this.copy(onBlurHandler = f)

  private def getFormInputElement: Option[HTMLInputElement] = {
    dom.document.getElementById(formId) match {
      case e: HTMLInputElement => Some(e)
      case _                   => None
    }
  }

  def getText: String = getFormInputElement.map(_.value).getOrElse("")
  def setText(s: String): Unit = getFormInputElement.foreach { el =>
    el.value = s
  }

  def focus: Unit = getFormInputElement.foreach { el =>
    el.focus()
    el.select()
  }

  def blur: Unit = getFormInputElement.foreach { el =>
    el.blur()
  }

  override def render: RxElement = div(
    id  -> elementId,
    cls -> "input-group",
    (cls += "input-group-sm").when(isSmall),
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
      cls -> s"form-control",
      (cls += "from-control-sm").when(isSmall),
      tpe         -> "text",
      placeholder -> placeholderText,
      aria.label  -> "search input",
      onkeyup -> { e: KeyboardEvent =>
        if (e.keyCode == KeyCode.Enter) {
          onEnterHandler(getText)
        } else {
          onChangeHandler(getText)
        }
      },
      onblur -> { e: Event => onBlurHandler() }
    )
  )
}
