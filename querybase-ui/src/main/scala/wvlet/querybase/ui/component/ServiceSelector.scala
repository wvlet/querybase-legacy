package wvlet.querybase.ui.component

import wvlet.airframe.rx.RxStreamCache
import wvlet.airframe.rx.html.RxElement
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.ServiceCatalogApi.Service
import wvlet.querybase.api.frontend.ServiceJSClientRx
import wvlet.querybase.ui.component.common.{Selector, SelectorItem}

import java.util.concurrent.TimeUnit

class ServiceSelector(rpcRxClient: ServiceJSClientRx) extends RxElement with LogSupport {

  private val selector = new Selector("Service", Seq.empty)
  private val serviceListCache: RxStreamCache[Seq[Service]] = {
    rpcRxClient.FrontendApi.serviceCatalog().cache.expireAfterWrite(5, TimeUnit.MINUTES)
  }

  def getSelectedService: Option[Service] = {
    serviceListCache.getCurrent.map { lst =>
      lst(selector.selectedIndex)
    }
  }

  override def render: RxElement = {
    serviceListCache.map { lst =>
      selector.setItems(lst.map(x => SelectorItem(x.name, x.name)))
      selector
    }
  }
}
