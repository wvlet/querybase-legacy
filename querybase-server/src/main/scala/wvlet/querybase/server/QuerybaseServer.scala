package wvlet.querybase.server

import wvlet.airframe.Design
import wvlet.airframe.http.Router
import wvlet.airframe.http.finagle.{Finagle, FinagleServer, FinagleSyncClient}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.ServiceSyncClient
import wvlet.querybase.api.v1.ServiceApi
import wvlet.querybase.server.api.{QueryLogApiImpl, StaticContentApi}
import wvlet.querybase.store.{QueryStorage, SQLiteQueryStorage}
import wvlet.airframe.http.Http
import wvlet.airframe.http.Http.SyncClient
import wvlet.airframe.http.HttpMessage.Request
import wvlet.airframe.http.HttpMessage.Response
import wvlet.querybase.server.api.code.ProjectApiImpl

/**
  */
object QuerybaseServer extends LogSupport {

  private[server] def router =
    Router
      .add[StaticContentApi]
      .add[ServiceApi]
      .add[QueryLogApiImpl]
      .add[ProjectApiImpl]

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
      .bind[SyncClient].toInstance(Http.client.withRetryContext(_.noRetry).newSyncClient(s"localhost:${port}"))
      .bind[QuerybaseSyncClient].toProvider { syncClient: SyncClient =>
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
