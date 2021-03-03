package wvlet.querybase.ui.component

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.querybase.ui.RPCService
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.frontend.code.ProjectApi.Project

/**
  */
trait ProjectSelector extends RxElement with RPCService {

  private def getProjects = {
    Rx.fromFuture(rpc(_.code.ProjectApi.listProject()))
  }

  def render: RxElement = {
    div(
      cls -> "dropdown p",
      div(
        h5(cls -> "dropdown-header pl-1", "Projects"),
        getProjects.map { projectList =>
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
