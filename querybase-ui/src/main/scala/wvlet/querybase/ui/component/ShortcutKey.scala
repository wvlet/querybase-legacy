package wvlet.querybase.ui.component

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{HTMLInputElement, HTMLTextAreaElement, KeyboardEvent}
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.log.LogSupport

import java.util.concurrent.TimeUnit
import scala.scalajs.js

/** */
case class ShortcutKeyDef(
    keyCode: Int,
    alt: Boolean = false,
    ctrl: Boolean = false,
    shift: Boolean = false,
    meta: Boolean = false,
    description: String,
    handler: dom.KeyboardEvent => Unit
) {
  def hasMatch(e: KeyboardEvent): Boolean = {
    e.keyCode == keyCode && e.altKey == alt && e.ctrlKey == ctrl && e.metaKey == meta && e.shiftKey == shift
  }
}

class ShortcutKeys(name: String, keys: Seq[ShortcutKeyDef] = Seq.empty, ignoreTextArea: Boolean = true)
    extends RxElement
    with LogSupport {

  private val keyEvent = Rx.variable[KeyboardEvent](new KeyboardEvent(""))
  // We need to explicitly create js.Function1 object to have a stable pointer to the function.
  // Without this cast, removeEventListener won't work
  protected val onKeyDown: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
    keyEvent := e
  }

  override def beforeRender: Unit = {
    info(s"[${name}] Add shortcuts")
    dom.document.addEventListener("keydown", onKeyDown, false)
  }

  override def beforeUnmount: Unit = {
    info(s"[${name}] Remove shortcuts")
    dom.document.removeEventListener("keydown", onKeyDown, false)
  }

  private def isTextArea(e: KeyboardEvent): Boolean = {
    e.target match {
      case input: HTMLInputElement   => true
      case text: HTMLTextAreaElement => true
      case _                         => false
    }
  }

  override def render: RxElement =
    keyEvent.throttleLast(200, TimeUnit.MILLISECONDS).map { e =>
      trace(s"[${name}] Key is pressed: ${e.key}:${e.keyCode}, meta:${e.metaKey}, alt:${e.altKey}, ${e.ctrlKey}")
      if (e.keyCode == KeyCode.Escape || !(ignoreTextArea && isTextArea(e))) {
        keys.find(key => key.hasMatch(e)).foreach { keyDef =>
          debug(s"Found a match: ${keyDef}")
          e.preventDefault()
          keyDef.handler(e)
        }
      }
      None
    }
}
