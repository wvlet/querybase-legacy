package wvlet.querybase.server

import wvlet.airframe.Design
import wvlet.airframe.http.Router
import wvlet.airframe.http.finagle.{Finagle, FinagleServer, FinagleSyncClient}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.v1.ServiceApi
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.server.api.{QueryApiImpl, StaticContentApi}

/**
  */
object QuerybaseServer extends LogSupport {

  private[server] def router =
    Router
      .add[StaticContentApi]
      .add[ServiceApi]
      .add[QueryApiImpl]

  def design(config: QuerybaseServerConfig): Design =
    Design.newDesign
      .bind[QuerybaseServerConfig].toInstance(config)
      .add(
        Finagle.server
          .withName("querybase")
          .withRouter(router)
          .withPort(config.port)
          .design
      )
      .bind[QuerybaseServer].toEagerSingleton

  private[querybase] def testDesign = {
    val port = IOUtil.randomPort
    design(QuerybaseServerConfig(port = port))
      .bind[FinagleSyncClient].toInstance(Finagle.client.newSyncClient(s"localhost:${port}"))
  }
}

case class QuerybaseServerConfig(port: Int = 8080)

class QuerybaseServer(server: FinagleServer) {

  def waitForTermination: Unit = {
    server.waitServerTermination
  }

}
