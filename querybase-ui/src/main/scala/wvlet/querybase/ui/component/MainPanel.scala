package wvlet.querybase.ui.component

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.ui.RPCService
import wvlet.airframe._
import wvlet.querybase.ui.component.notebook.NotebookElement

/**
  */
trait MainPanel extends RxElement {

  private val navbar     = bind[NavBar]
  private val notebookUI = bind[NotebookUI]

  override def render = {
    div(
      navbar,
      div(
        cls   -> "container-fluid",
        style -> """padding-top: 40px; """,
        notebookUI
      )
    )
  }
}
