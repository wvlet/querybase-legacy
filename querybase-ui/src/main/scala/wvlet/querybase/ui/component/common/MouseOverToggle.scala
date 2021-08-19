package wvlet.querybase.ui.component.common

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.{Rx, RxVar}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{div, onmouseout, onmouseover}

/** */
object MouseOverToggle {

  def apply(toggleOnHover: RxVar[Boolean]): RxElement = {
    Seq(
      onmouseover -> { e: MouseEvent => toggleOnHover := true },
      onmouseout  -> { e: MouseEvent => toggleOnHover := false }
    )
  }
}
