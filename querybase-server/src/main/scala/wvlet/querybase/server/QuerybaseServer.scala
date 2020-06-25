package wvlet.querybase.server

import com.twitter.finagle.http.{Request, Response}
import wvlet.airframe.Design
import wvlet.airframe.http.Router
import wvlet.airframe.http.finagle.{Finagle, FinagleServer, FinagleSyncClient}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.ServiceSyncClient
import wvlet.querybase.api.v1.ServiceApi
import wvlet.querybase.api.v1.ServiceApi.ServiceInfo
import wvlet.querybase.server.api.{QueryLogApiImpl, StaticContentApi}
import wvlet.querybase.store.{QueryStorage, SQLiteQueryStorage}

/**
  */
object QuerybaseServer extends LogSupport {

  private[server] def router =
    Router
      .add[StaticContentApi]
      .add[ServiceApi]
      .add[QueryLogApiImpl]

  type QuerybaseSyncClient = ServiceSyncClient[Request, Response]

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
      .bind[QueryStorage].to[SQLiteQueryStorage]

  private[querybase] def testDesign = {
    val port = IOUtil.randomPort
    design(QuerybaseServerConfig(port = port))
      .bind[FinagleSyncClient].toInstance(Finagle.client.newSyncClient(s"localhost:${port}"))
      .bind[QuerybaseSyncClient].toProvider { syncClient: FinagleSyncClient =>
        new ServiceSyncClient(syncClient)
      }
  }

}

case class QuerybaseServerConfig(port: Int = 8080)

class QuerybaseServer(server: FinagleServer) {

  def waitForTermination: Unit = {
    server.waitServerTermination
  }

}
