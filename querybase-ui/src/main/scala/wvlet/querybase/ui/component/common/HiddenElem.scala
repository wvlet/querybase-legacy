package wvlet.querybase.ui.component.common

import wvlet.airframe.rx.RxVar
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{s, span, style}

/**
  */
case class HiddenElem(visibilityToggle: RxVar[Boolean], elem: RxElement) extends RxElement {
  override def render: RxElement = span(
    visibilityToggle.map { x =>
      style -> s"visibility: ${if (x) "visible" else "hidden"};"
    },
    elem
  )
}
