package wvlet.querybase.server.frontend

import io.grpc.ManagedChannelBuilder
import wvlet.airframe.Design
import wvlet.airframe.http.Http.SyncClient
import wvlet.airframe.http.HttpMessage.{Request, Response}
import wvlet.airframe.http.finagle.{Finagle, FinagleServer}
import wvlet.airframe.http.{Http, Router, ServerAddress}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.backend.ServiceGrpc
import wvlet.querybase.api.frontend.ServiceSyncClient
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient
import wvlet.querybase.server.frontend.code.{NotebookApiImpl, ProjectApiImpl}
import wvlet.querybase.store.{QueryStorage, SQLiteQueryStorage}

case class FrontendServerConfig(
    port: Int = 8080,
    coordinatorAddress: ServerAddress = ServerAddress("localhost:8081")
)

class FrontendServer(server: FinagleServer) {
  def waitForTermination: Unit = {
    server.waitServerTermination
  }
}

object FrontendServer extends LogSupport {

  private[server] def router =
    Router
      .add[StaticContentApi]
      .add[FrontendApiImpl]
      .add[QueryLogApiImpl]
      .add[ProjectApiImpl]
      .add[NotebookApiImpl]

  type FrontendClient = ServiceSyncClient[Request, Response]

  def design(config: FrontendServerConfig): Design = {
    Design.newDesign
      .bind[FrontendServerConfig].toInstance(config)
      .add(
        Finagle.server
          .withName("querybase-frontend")
          .withRouter(router)
          .withPort(config.port)
          .design
      )
      .bind[FrontendServer].toEagerSingleton
      .bind[QueryStorage].to[SQLiteQueryStorage]
      .bind[CoordinatorClient].toLazyInstance {
        val channel = ManagedChannelBuilder.forTarget(config.coordinatorAddress.hostAndPort).usePlaintext().build
        ServiceGrpc.newSyncClient(channel)
      }
  }

  private[querybase] def testDesign = {
    val port = IOUtil.randomPort
    design(FrontendServerConfig(port = port, coordinatorAddress = ServerAddress("localhost:8081")))
      .bind[SyncClient].toInstance(Http.client.withRetryContext(_.noRetry).newSyncClient(s"localhost:${port}"))
      .bind[FrontendClient].toProvider { syncClient: SyncClient =>
        new ServiceSyncClient(syncClient)
      }
  }
}
