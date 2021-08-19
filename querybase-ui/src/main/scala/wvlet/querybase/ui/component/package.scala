package wvlet.querybase.ui

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.raw.HTMLElement

/** */
package object component {

  def DO_NOTHING[Input]: Input => Unit = { x: Any => }

  def findHTMLElement(id: String): Option[HTMLElement] = {
    dom.document.getElementById(id) match {
      case e: HTMLElement => Some(e)
      case _              => None
    }
  }

  implicit class RichEvent(val e: Event) extends AnyVal {
    def getSourceElement: Option[HTMLElement] = {
      e.target match {
        case h: HTMLElement => Some(h)
        case _              => None
      }
    }
    def getCurrentTarget: Option[HTMLElement] = {
      e.currentTarget match {
        case h: HTMLElement => Some(h)
        case _              => None
      }
    }
  }

  // TODO Move to airframe-rx-html
  //implicit def elemToRx(e: RxElement): Rx[RxElement] = Rx.const(e)
}
