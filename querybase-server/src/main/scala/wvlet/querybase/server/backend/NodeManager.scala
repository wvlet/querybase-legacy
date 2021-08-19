package wvlet.querybase.server.backend

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{Node, NodeInfo}

import java.net.InetAddress
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/** */
class NodeManager(coordinatorConfig: CoordinatorConfig) extends LogSupport {
  import scala.jdk.CollectionConverters._

  private val self: Node = {
    val localHost = InetAddress.getLocalHost
    val localAddr = s"${localHost.getHostAddress}:${coordinatorConfig.serverAddress.port}"
    Node(name = coordinatorConfig.name, address = localAddr, isCoordinator = true, startedAt = Instant.now())
  }

  private val heartBeatRecord = new ConcurrentHashMap[Node, Instant]().asScala

  def heartBeat(node: Node): Unit = {
    heartBeatRecord.getOrElseUpdate(
      node, {
        info(s"Joined: ${node}")
        Instant.now()
      }
    )
    heartBeatRecord.put(node, Instant.now())
  }

  def listNodes: Seq[NodeInfo] = {
    val b = Seq.newBuilder[NodeInfo]
    b += NodeInfo(self, Instant.now())
    heartBeatRecord.foreach { case (n, hb) => b += NodeInfo(n, hb) }
    b.result()
  }

}
