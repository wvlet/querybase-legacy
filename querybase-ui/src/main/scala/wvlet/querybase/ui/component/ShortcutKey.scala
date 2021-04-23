package wvlet.querybase.ui.component

import org.scalajs.dom
import org.scalajs.dom.raw.KeyboardEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{div, span}
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/**
  */
case class ShortcutKeyDef(
    key: String,
    keyCode: Int,
    description: String,
    handler: dom.KeyboardEvent => Unit
)

class ShortcutKeys() extends RxElement with LogSupport {

  private val keyEvent = Rx.optionVariable[KeyboardEvent](None)
  // We need to explicitly create js.Function1 object to have a stable pointer to the function.
  // Without this cast, removeEventListener won't work
  protected val onKeyDown: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
    keyEvent := Some(e)
  }

  override def beforeRender: Unit = {
    info(s"Add shortcuts")
    dom.document.addEventListener("keydown", onKeyDown, false)
  }

  override def beforeUnmount: Unit = {
    info(s"Remove shortcuts")
    dom.document.removeEventListener("keydown", onKeyDown, false)
  }

  override def render: RxElement =
    keyEvent.map { e =>
      trace(s"Key is pressed: ${e.key}:${e.keyCode}")
      // Nothing to render
      None
    }
}

object ShortcutKey {}
