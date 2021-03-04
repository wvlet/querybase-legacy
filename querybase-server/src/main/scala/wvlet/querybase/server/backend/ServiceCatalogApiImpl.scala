package wvlet.querybase.server.backend

import wvlet.airframe.json.Json
import wvlet.airframe.surface.secret
import wvlet.log.io.Resource
import wvlet.querybase.api.backend.v1.ServiceCatalogApi
import wvlet.querybase.api.backend.v1.ServiceCatalogApi.Service

case class ServiceCatalog(services: Seq[ServiceDef])
case class ServiceDef(id: String, serviceType: String, name: String, description: String, @secret properties: Json)

class ServiceCatalogApiImpl(catalog: ServiceCatalog) extends ServiceCatalogApi {

  override def listServices: Seq[ServiceCatalogApi.Service] = {
    catalog.services.map { x =>
      Service(x.id, x.serviceType, x.name, x.description)
    }
  }
}
