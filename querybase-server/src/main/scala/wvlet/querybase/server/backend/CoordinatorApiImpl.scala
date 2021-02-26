package wvlet.querybase.server.backend

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi
import wvlet.querybase.api.backend.v1.CoordinatorApi.Node

import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
  */
class CoordinatorApiImpl(nodeManager: NodeManager) extends CoordinatorApi with LogSupport {
  import CoordinatorApi._

  override def listNodes: Seq[Node] = {
    nodeManager.listNodes
  }

  override def register(node: Node): RegisterResponse = {
    nodeManager.heartBeat(node)
    RegisterResponse()
  }

}

class NodeManager(coordinatorConfig: CoordinatorConfig) extends LogSupport {
  import scala.jdk.CollectionConverters._

  private val self: Node = {
    val localHost = InetAddress.getLocalHost
    val localAddr = s"${localHost.getHostAddress}:${coordinatorConfig.serverAddress.port}"
    Node(name = "coordinator", address = localAddr, isCoordinator = true)
  }

  private val heartBeatRecord = new ConcurrentHashMap[Node, Long]().asScala

  def heartBeat(node: Node): Unit = {
    heartBeatRecord.getOrElseUpdate(
      node, {
        info(s"Joined: ${node}")
        System.currentTimeMillis()
      }
    )
    heartBeatRecord.put(node, System.currentTimeMillis())
  }

  def listNodes: Seq[Node] = self +: heartBeatRecord.keys.toSeq

}
