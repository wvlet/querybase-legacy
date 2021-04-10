package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.{Event, HTMLInputElement, MouseEvent}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport

/**
  */
class SchemaForm extends RxElement with LogSupport {
  private var _text: String = ""

  def getText: Option[String] = {
    _text.trim match {
      case s if s.isEmpty => None
      case other          => Some(other)
    }
  }

  override def render = form(
    cls -> "form-inline",
    div(
      cls -> "form-group",
      label(cls -> "mr-2", small("Schema:")),
      input(
        cls         -> "form-control form-control-sm",
        tpe         -> "text",
        placeholder -> "(database schema)",
        onchange -> { e: KeyboardEvent =>
          e.target match {
            case e: HTMLInputElement =>
              Option(e.value).foreach(_text = _)
          }
        }
      )
    )
  )
}
