package wvlet.querybase.server.frontend

import wvlet.airframe.Design
import wvlet.airframe.http.Http.SyncClient
import wvlet.airframe.http.finagle.{Finagle, FinagleServer}
import wvlet.airframe.http.{Http, Router}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.frontend.ServiceApi
import wvlet.querybase.server.frontend.code.{NotebookApiImpl, ProjectApiImpl}
import wvlet.querybase.store.{QueryStorage, SQLiteQueryStorage}

case class QuerybaseServerConfig(port: Int = 8080)

class FrontendServer(server: FinagleServer) {
  def waitForTermination: Unit = {
    server.waitServerTermination
  }
}

object FrontendServer extends LogSupport {

  private[server] def router =
    Router
      .add[StaticContentApi]
      .add[ServiceApi]
      .add[QueryLogApiImpl]
      .add[ProjectApiImpl]
      .add[NotebookApiImpl]

  def design(config: QuerybaseServerConfig): Design =
    Design.newDesign
      .bind[QuerybaseServerConfig].toInstance(config)
      .add(
        Finagle.server
          .withName("querybase-frontend")
          .withRouter(router)
          .withPort(config.port)
          .design
      )
      .bind[FrontendServer].toEagerSingleton
      .bind[QueryStorage].to[SQLiteQueryStorage]

  private[querybase] def testDesign = {
    val port = IOUtil.randomPort
    design(QuerybaseServerConfig(port = port))
      .bind[SyncClient].toInstance(Http.client.withRetryContext(_.noRetry).newSyncClient(s"localhost:${port}"))
  }
}
