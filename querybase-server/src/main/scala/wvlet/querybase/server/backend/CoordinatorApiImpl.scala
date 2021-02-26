package wvlet.querybase.server.backend

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi
import wvlet.querybase.server.backend.BackendServer.CoordinatorConfig

import java.net.InetAddress

/**
  */
class CoordinatorApiImpl(coordinatorConfig: CoordinatorConfig) extends CoordinatorApi with LogSupport {
  import CoordinatorApi._

  private val self: Node = {
    val localHost = InetAddress.getLocalHost
    val localAddr = s"${localHost.getHostAddress}:${coordinatorConfig.serverAddress.port}"
    Node(name = "coordinator", address = localAddr, isCoordinator = true)
  }

  override def listNodes: Seq[Node] = {
    Seq(self)
  }

  override def register(node: Node): RegisterResponse = {
    synchronized {
      info(s"Add ${node}")
    }
    RegisterResponse()
  }

}
