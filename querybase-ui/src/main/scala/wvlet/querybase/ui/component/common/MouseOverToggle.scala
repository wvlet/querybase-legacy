package wvlet.querybase.ui.component.common

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.{Rx, RxVar}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{div, onmouseout, onmouseover}

/**
  */
case class MouseOverToggle(toggleOnHover: RxVar[Boolean], elem: RxElement) extends RxElement {

  override def render: RxElement = {
    div(
      onmouseover -> { e: MouseEvent => toggleOnHover := true },
      onmouseout  -> { e: MouseEvent => toggleOnHover := false },
      elem
    )
  }
}
