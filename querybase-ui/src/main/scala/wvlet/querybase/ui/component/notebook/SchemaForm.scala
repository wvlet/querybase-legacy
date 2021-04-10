package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.{HTMLInputElement, MouseEvent}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

/**
  */
class SchemaForm extends RxElement {
  private var _text: String = "information_schema"

  def text: String = _text.trim

  override def render = form(
    cls -> "form-inline",
    div(
      cls -> "form-group",
      label(cls -> "mr-2", small("Schema:")),
      input(
        cls         -> "form-control form-control-sm",
        tpe         -> "text",
        placeholder -> _text,
        onchange -> { e: MouseEvent =>
          e.target match {
            case e: HTMLInputElement =>
              Option(e.textContent).foreach(_text = _)
          }
        }
      )
    )
  )
}
