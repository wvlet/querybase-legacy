package wvlet.querybase.ui

import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.HTMLElement

/**
  */
package object component {
  implicit class RichMouseEvent(val e: MouseEvent) extends AnyVal {
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
}
