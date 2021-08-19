package wvlet.querybase.ui.component.page

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.RPCService

/** */
trait HomePage extends RxElement with RPCService {
  override def render: RxElement = div(
    "Hello Querybase!"
  )
}
