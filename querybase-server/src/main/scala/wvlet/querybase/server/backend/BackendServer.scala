package wvlet.querybase.server.backend

import io.grpc.{Channel, ManagedChannel, ManagedChannelBuilder}
import wvlet.airframe.{Design, newDesign}
import wvlet.airframe.http.{Router, ServerAddress}
import wvlet.airframe.http.grpc.{GrpcServerConfig, gRPC}
import wvlet.log.io.IOUtil
import wvlet.querybase.api.backend.ServiceGrpc
import wvlet.querybase.api.backend.v1.CoordinatorApi.Node
import wvlet.querybase.api.backend.v1.ServerInfoApi

import java.net.InetAddress

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
object BackendServer {

  type BackendClient      = ServiceGrpc.SyncClient
  type CoordinatorChannel = ManagedChannel

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

  private def baseDesign(config: NodeConfig): Design =
    newDesign.bind[NodeConfig].toInstance(config)

  def coordinatorDesign(config: NodeConfig): Design = baseDesign(config)
    .add(coordinatorServer(config).design)

  def workerDesign(config: NodeConfig): Design = {
    val coordinatorAddress = config.coordinatorAddress.get

    baseDesign(config)
      .add(workerServer(config).design)
      .bind[CoordinatorChannel].toInstance(
        ManagedChannelBuilder.forAddress(coordinatorAddress.host, coordinatorAddress.port).build()
      )
      .onShutdown(_.shutdownNow())
      .bind[BackendClient].toProvider { channel: CoordinatorChannel => ServiceGrpc.newSyncClient(channel) }
      .bind[WorkerService].toEagerSingleton
  }

  def testDesign: Design = {
    val port               = IOUtil.randomPort
    val coordiantorAddress = ServerAddress(s"localhost:${port}")
    val workerPort: Int    = IOUtil.randomPort
    val coordinatorConfig = NodeConfig(
      name = "test-coordinator",
      serverAddress = coordiantorAddress
    )
    val workerConfig = NodeConfig(
      name = "test-worker-1",
      serverAddress = ServerAddress(s"localhost:${workerPort}"),
      coordinatorAddress = Some(coordiantorAddress)
    )
    baseDesign(coordinatorConfig)
      .add(coordinatorServer(coordinatorConfig).designWithChannel)
      .add(workerDesign(workerConfig))
      .bind[BackendClient].toProvider { channel: Channel => ServiceGrpc.newSyncClient(channel) }
      .withProductionMode
  }

  private[backend] def selfNode(nodeConfig: NodeConfig): Node = {
    Node(
      name = nodeConfig.name,
      nodeConfig.serverAddress.toString(),
      isCoordinator = nodeConfig.isCoordinator
    )
  }

}
