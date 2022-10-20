package wvlet.querybase.server.frontend

import io.grpc.ManagedChannelBuilder
import wvlet.airframe.Design
import wvlet.airframe.http.HttpMessage.{Request, Response}
import wvlet.airframe.http.client.SyncClient
import wvlet.airframe.http.finagle.{Finagle, FinagleServer}
import wvlet.airframe.http.{Http, Router, ServerAddress}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.backend.ServiceGrpc
import wvlet.querybase.api.frontend.ServiceRPC.RPCSyncClient
import wvlet.querybase.api.frontend.ServiceRPC
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
      .add[AuthFilter].andThen(
        Router
          .add[FrontendApiImpl]
          .add[QueryLogApiImpl]
          .add[ProjectApiImpl]
          .add[NotebookApiImpl]
      )
      .add[StaticContentApi]

  type FrontendClient = RPCSyncClient

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
      .bind[AuthFilter].to[GoogleAuthFilter]
      .bind[CoordinatorClient].toInstance {
        val channel = ManagedChannelBuilder
          .forTarget(config.coordinatorAddress.hostAndPort)
          .maxInboundMessageSize(128 * 1024 * 1024)
          .usePlaintext().build
        ServiceGrpc.newSyncClient(channel)
      }

  }

  private[querybase] def testDesign: Design = {
    val port = IOUtil.randomPort
    design(FrontendServerConfig(port = port, coordinatorAddress = ServerAddress("localhost:8081")))
      .bind[SyncClient].toInstance(Http.client.withRetryContext(_.noRetry).newSyncClient(s"localhost:${port}"))
      .bind[FrontendClient].toProvider { (syncClient: SyncClient) =>
        ServiceRPC.newRPCSyncClient(syncClient)
      }
      // Disable GoogleAuth for testing
      .bind[AuthFilter].toInstance(NoAuthFilter)
  }
}
