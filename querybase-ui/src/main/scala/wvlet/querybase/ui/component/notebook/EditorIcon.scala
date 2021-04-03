package wvlet.querybase.ui.component.notebook

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport

/**
  */
class EditorIcon(name: String, iconClass: String, onClick: MouseEvent => Unit) extends RxElement with LogSupport {
  import wvlet.querybase.ui.component._

  private val baseCls = s"fa ${iconClass} p-1"

  override def render: RxElement =
    i(
      cls     -> s"${baseCls} text-secondary",
      title   -> name,
      onclick -> { e: MouseEvent => onClick(e) },
      onmouseover -> { e: MouseEvent =>
        e.getCurrentTarget.foreach { el =>
          el.className = s"${baseCls} text-white bg-primary"
        }
      },
      onmouseout -> { e: MouseEvent =>
        e.getCurrentTarget.foreach { el =>
          el.className = s"${baseCls} text-secondary"
        }
      }
    )
}
