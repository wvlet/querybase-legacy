package wvlet.querybase.ui.component

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.ui.RPCService

/**
  */
class MainPanel extends RxElement with RPCService {

  private val serviceInfo = Rx.optionVariable[ServiceInfo](None)

  rpc(_.ServiceApi.serviceInfo().map { x =>
    serviceInfo.set(Some(x))
  })

  override def render: RxElement = {
    serviceInfo.map { x =>
      div(
        s"Hello Querybase: ${x}"
      )
    }
  }
}
