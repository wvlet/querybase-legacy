package wvlet.querybase.ui.component

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.querybase.ui.RPCService
import wvlet.airframe.rx.html.all._

/**
  */
trait ProjectSelector extends RxElement with RPCService {

  def render: RxElement = {
    div(
      cls -> "dropdown p",
      div(
        h5(cls -> "dropdown-header pl-1", "Projects"),
        Rx.fromFuture(rpc(_.ProjectApi.listProject())).map { projectList =>
          projectList.map { x =>
            a(
              cls     -> "dropdown-item pl-1",
              href    -> "#",
              onclick -> { e: MouseEvent => },
              x.name
            )
          }
        }
      )
    )
  }
}
