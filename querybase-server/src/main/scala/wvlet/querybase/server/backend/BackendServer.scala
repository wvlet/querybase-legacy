package wvlet.querybase.server.backend

import io.grpc.{Channel, ManagedChannel, ManagedChannelBuilder}
import wvlet.airframe.{Design, Session, newDesign}
import wvlet.airframe.http.{Router, ServerAddress}
import wvlet.airframe.http.grpc.{GrpcServer, GrpcServerConfig, gRPC}
import wvlet.log.LogSupport
import wvlet.log.io.IOUtil
import wvlet.log.io.IOUtil.withResource
import wvlet.querybase.api.backend.ServiceGrpc
import wvlet.querybase.api.backend.v1.CoordinatorApi.Node
import wvlet.querybase.api.backend.v1.ServerInfoApi

import java.net.{InetAddress, ServerSocket}

case class NodeConfig(
    name: String,
    // self-address
    serverAddress: ServerAddress,
    // Set this for connecting to the coordinator
    coordinatorAddress: Option[ServerAddress] = None
) {
  def isCoordinator: Boolean = coordinatorAddress.isEmpty
  def port: Int              = serverAddress.port
}

/**
  */
object BackendServer extends LogSupport {

  type CoordinatorClient  = ServiceGrpc.SyncClient
  type CoordinatorChannel = ManagedChannel
  type CoordinatorConfig  = NodeConfig
  type WorkerConfig       = NodeConfig

  type CoordinatorServer = GrpcServer
  type WorkerServer      = GrpcServer

  def coordinatorRouter = Router.add[ServerInfoApi].add[CoordinatorApiImpl]
  def workerRouter      = Router.add[ServerInfoApi]

  private def coordinatorServer(config: NodeConfig): GrpcServerConfig =
    gRPC.server
      .withName(config.name)
      .withPort(config.port)
      .withRouter(coordinatorRouter)

  private def workerServer(config: NodeConfig): GrpcServerConfig =
    gRPC.server
      .withName(config.name)
      .withPort(config.port)
      .withRouter(workerRouter)

  def coordinatorDesign(config: CoordinatorConfig): Design = {
    newDesign
      .bind[CoordinatorConfig].toInstance(config)
      .bind[CoordinatorServer].toProvider { session: Session => coordinatorServer(config).newServer(session) }
  }

  def workerDesign(config: WorkerConfig): Design = {
    val coordinatorAddress = config.coordinatorAddress.get

    newDesign
      .bind[WorkerConfig].toInstance(config)
      .bind[WorkerServer].toProvider { session: Session => workerServer(config).newServer(session) }
      .bind[CoordinatorChannel].toInstance(
        // TODO: Wait until coordinator starts
        ManagedChannelBuilder.forAddress(coordinatorAddress.host, coordinatorAddress.port).usePlaintext().build()
      )
      .onShutdown(_.shutdownNow())
      .bind[CoordinatorClient].toProvider { channel: CoordinatorChannel => ServiceGrpc.newSyncClient(channel) }
      .bind[WorkerService].toEagerSingleton
  }

  private def randomPort(num: Int): Seq[Int] = {
    val sockets = (0 until num).map(i => new ServerSocket(0))
    val ports   = sockets.map(_.getLocalPort).toIndexedSeq
    sockets.foreach(_.close())
    ports
  }

  def testDesign: Design = {
    val port               = randomPort(2)
    val coordinatorAddress = ServerAddress(s"localhost:${port(0)}")
    val coordinatorConfig = NodeConfig(
      name = "test-coordinator",
      serverAddress = coordinatorAddress
    )
    val workerConfig = NodeConfig(
      name = "test-worker-1",
      serverAddress = ServerAddress(s"localhost:${port(1)}"),
      coordinatorAddress = Some(coordinatorAddress)
    )

    coordinatorDesign(coordinatorConfig)
      .add(workerDesign(workerConfig))
      // Add a dependency to CoordinatorServer to wait for the startup
      .bind[CoordinatorClient].toProvider { (coordinatorServer: CoordinatorServer, channel: CoordinatorChannel) =>
        ServiceGrpc.newSyncClient(channel)
      }
  }

  private[backend] def selfNode(nodeConfig: NodeConfig): Node = {
    Node(
      name = nodeConfig.name,
      nodeConfig.serverAddress.toString(),
      isCoordinator = nodeConfig.isCoordinator
    )
  }

}
