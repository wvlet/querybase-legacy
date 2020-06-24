package wvlet.querybase.ui.component

import wvlet.airframe.http.rx.Rx
import wvlet.airframe.http.rx.html.RxElement
import wvlet.airframe.http.rx.html.all._
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.ui.RPCService

/**
  *
  */
class MainPanel extends RxElement with RPCService {

  private val serviceInfo = Rx.variable[Option[ServiceInfo]](None)

  rpc(_.serviceApi.serviceInfo().map { x =>
    serviceInfo.set(Some(x))
  })

  override def render: RxElement = {
    serviceInfo.map { x =>
      div(
        s"Hello Querybase: ${x.getOrElse("")}"
      )
    }
  }
}
