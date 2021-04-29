package wvlet.querybase.server.backend

import io.grpc.ManagedChannelBuilder
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.http.grpc.{GrpcServer, GrpcServerConfig, gRPC}
import wvlet.airframe.http.{Router, ServerAddress}
import wvlet.airframe.{Design, Session, newDesign}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.querybase.api.backend.ServiceGrpc
import wvlet.querybase.api.backend.v1.ServerInfoApi
import wvlet.querybase.server.backend.api.{
  CoordinatorApiImpl,
  SearchApiImpl,
  ServiceCatalog,
  ServiceCatalogApiImpl,
  WorkerApiImpl
}
import wvlet.querybase.server.backend.query.QueryExecutorConfig

import java.io.File
import java.net.ServerSocket
import java.util.concurrent.Executors

case class CoordinatorConfig(
    name: String = "coordinator",
    // self-address
    serverAddress: ServerAddress
) {
  def port: Int = serverAddress.port
}

case class WorkerConfig(
    name: String = "worker-1",
    serverAddress: ServerAddress,
    coordinatorAddress: ServerAddress
) {
  def port: Int = serverAddress.port
}

/**
  */
object BackendServer extends LogSupport {

  type CoordinatorClient = ServiceGrpc.SyncClient
  type CoordinatorServer = GrpcServer
  type WorkerServer      = GrpcServer

  def coordinatorRouter = Router
    .add[ServerInfoApi]
    .add[CoordinatorApiImpl]
    .add[ServiceCatalogApiImpl]
    .add[SearchApiImpl]

  def workerRouter = Router
    .add[ServerInfoApi]
    .add[WorkerApiImpl]

  private def coordinatorServer(config: CoordinatorConfig): GrpcServerConfig =
    gRPC.server
      .withName(config.name)
      .withPort(config.port)
      .withExecutorServiceProvider { config => Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()) }
      .withRouter(coordinatorRouter)

  private def workerServer(config: WorkerConfig): GrpcServerConfig =
    gRPC.server
      .withName(config.name)
      .withPort(config.port)
      .withExecutorServiceProvider { config => Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()) }
      .withRouter(workerRouter)

  def coordinatorDesign(config: CoordinatorConfig): Design = {
    newDesign
      .bind[CoordinatorConfig].toInstance(config)
      .bind[CoordinatorServer].toProvider { session: Session => coordinatorServer(config).newServer(session) }
      .bind[ServiceCatalog].toInstance {
        val file = new File(".querybase/services.json")
        if (!file.exists) {
          ServiceCatalog(Seq.empty)
        } else {
          val json     = IOUtil.readAsString(file)
          val services = MessageCodec.of[ServiceCatalog].fromJson(json)
          services
        }
      }
  }

  def workerDesign(config: WorkerConfig): Design = {
    WorkerService.design
      .bind[WorkerConfig].toInstance(config)
      .bind[WorkerServer].toProvider { session: Session => workerServer(config).newServer(session) }
      .bind[WorkerService].toSingleton
      .bind[QueryExecutorConfig].toSingleton
  }

  private[server] def randomPort(num: Int): Seq[Int] = {
    val sockets = (0 until num).map(i => new ServerSocket(0))
    val ports   = sockets.map(_.getLocalPort).toIndexedSeq
    sockets.foreach(_.close())
    ports
  }

  def testDesign: Design = {
    val port               = randomPort(2)
    val coordinatorAddress = ServerAddress(s"localhost:${port(0)}")
    val coordinatorConfig = CoordinatorConfig(
      name = "test-coordinator",
      serverAddress = coordinatorAddress
    )
    val workerConfig = WorkerConfig(
      name = "test-worker-1",
      serverAddress = ServerAddress(s"localhost:${port(1)}"),
      coordinatorAddress = coordinatorAddress
    )

    coordinatorDesign(coordinatorConfig)
      .add(workerDesign(workerConfig))
      .bind[CoordinatorClient].toProvider { (coordinatorServer: CoordinatorServer) =>
        val channel = ManagedChannelBuilder.forTarget(coordinatorAddress.toString()).usePlaintext().build()
        ServiceGrpc.newSyncClient(channel)
      }
      .bind[WorkerService].toEagerSingleton

  }
}
