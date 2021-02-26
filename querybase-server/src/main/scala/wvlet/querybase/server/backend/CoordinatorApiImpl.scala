package wvlet.querybase.server.backend

import wvlet.querybase.api.backend.v1.CoordinatorApi

import java.net.InetAddress

/**
  */
class CoordinatorApiImpl(backendServerConfig: NodeConfig) extends CoordinatorApi {
  import CoordinatorApi._

  private val self: Node = {
    val localHost = InetAddress.getLocalHost
    val localAddr = s"${localHost.getHostAddress}:${backendServerConfig.serverAddress.port}"
    Node(name = "coordinator", address = localAddr, isCoordinator = true)
  }

  override def listNodes: Seq[Node] = {
    Seq(self)
  }

  override def register(node: Node): Unit = {
    synchronized {}
  }

}
