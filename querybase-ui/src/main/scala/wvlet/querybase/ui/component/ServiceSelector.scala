package wvlet.querybase.ui.component

import wvlet.airframe.rx.RxStream
import wvlet.airframe.rx.html.RxElement
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.ServiceCatalogApi.Service
import wvlet.querybase.api.frontend.ServiceJSClientRx
import wvlet.querybase.ui.component.common.{Selector, SelectorItem}

class ServiceSelector(rpcRxClient: ServiceJSClientRx) extends RxElement with LogSupport {

  private var serviceList: Seq[Service] = Seq.empty
  private var selector                  = new Selector("Service", Seq.empty)

  private val serviceListCache: RxStream[Seq[Service]] = rpcRxClient.FrontendApi
    .serviceCatalog().map { lst =>
      serviceList = lst
      lst
    }.cache

  def selectedService: Service = {
    serviceList(selector.getSelectedIndex)
  }

  override def render: RxElement = {
    serviceListCache.map { lst =>
      selector = new Selector("Service", lst.map(x => SelectorItem(x.name, x.name)))
      selector
    }
  }
}
