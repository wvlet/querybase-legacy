package wvlet.querybase.ui.component

import org.scalajs.dom
import org.scalajs.dom.raw.KeyboardEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.div
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

  // We need to explicitly create js.Function1 object to stabilize the function reference.
  // Without this cast, removeEventListener won't work
  protected val onKeyPress: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
    logger.info(s"Key is pressed: ${e.key}:${e.keyCode}")
  }

  override def beforeRender: Unit = {
    info(s"Add shortcuts")
    dom.document.addEventListener("keypress", onKeyPress, false)
  }

  override def beforeUnmount: Unit = {
    info(s"Remove shortcuts")
    dom.document.removeEventListener("keypress", onKeyPress, false)
  }

  override def render: RxElement = div()
}

object ShortcutKey {}
