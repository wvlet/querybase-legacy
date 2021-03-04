package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.querybase.api.backend.v1.ServiceCatalogApi.Service

@RPC
trait ServiceCatalogApi {
  def listServices: Seq[Service]
}

object ServiceCatalogApi {
  case class Service(id: String, serviceType: String, name: String, description: String)
}
