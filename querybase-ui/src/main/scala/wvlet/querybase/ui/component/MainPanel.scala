package wvlet.querybase.ui.component

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.ui.RPCService
import wvlet.airframe._

/**
  */
trait MainPanel extends RxElement {

  private val navbar = bind[NavBar]
  LoginProfile.init

  override def render = {
    div(
      navbar
    )
  }
}
