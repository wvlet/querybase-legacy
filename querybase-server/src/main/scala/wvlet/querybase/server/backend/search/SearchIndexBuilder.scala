package wvlet.querybase.server.backend.search

import wvlet.airframe.{Design, newDesign}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.NewQueryRequest
import wvlet.querybase.api.backend.v1.ServiceCatalogApi
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient
import wvlet.querybase.server.backend.search.SearchIndexBuilder.SearchIndexThreadManager

import java.util.concurrent.{ExecutorService, Executors}

/** */
class SearchIndexBuilder(
    indexDB: IndexDB,
    serviceCatalogApi: ServiceCatalogApi,
    coordinatorClient: CoordinatorClient,
    executor: SearchIndexThreadManager
) extends LogSupport {

  def build: Unit = {
    executor.submit(new Runnable {
      override def run(): Unit = {
        for (service <- serviceCatalogApi.listServices) {
          buildIndex(service)
        }
      }
    })
  }

  def buildIndex(service: ServiceCatalogApi.Service): Unit = {
    warn(s"Build indexes for ${service}")

    val queryId = coordinatorClient.v1.CoordinatorApi.newQuery(
      // Read all database names
      NewQueryRequest(
        query = "show schemas",
        serviceName = service.name,
        schema = None,
        // Read all databases
        limit = None
      )
    )

  }

}

object SearchIndexBuilder {
  type SearchIndexThreadManager = ExecutorService

  def design: Design = IndexDB
    .design()
    .bind[SearchIndexBuilder].toSingleton
    .onStart(_.build)
    .bind[SearchIndexThreadManager].toInstance(Executors.newSingleThreadExecutor())
    .onShutdown(_.shutdownNow())

}
