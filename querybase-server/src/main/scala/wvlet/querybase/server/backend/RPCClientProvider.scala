package wvlet.querybase.server.backend

import io.grpc.{ConnectivityState, ManagedChannel, ManagedChannelBuilder}
import wvlet.airframe.control.Control
import wvlet.airframe.http.ServerAddress
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.ServiceGrpc

import java.util.concurrent.ConcurrentHashMap
import scala.annotation.tailrec

/** */
class RPCClientProvider(workerConfig: WorkerConfig) extends LogSupport with AutoCloseable {

  import scala.jdk.CollectionConverters._

  private val clientHolder = new ConcurrentHashMap[String, ServiceGrpc.SyncClient]().asScala

  def getSyncClientFor(nodeAddress: ServerAddress): ServiceGrpc.SyncClient = {
    getSyncClientFor(nodeAddress.hostAndPort)
  }

  def getSyncClientFor(nodeAddress: String): ServiceGrpc.SyncClient = {
    clientHolder.getOrElseUpdate(
      nodeAddress, {
        val channel: ManagedChannel = ManagedChannelBuilder.forTarget(nodeAddress).usePlaintext().build()

        @tailrec
        def loop: Unit = {
          channel.getState(true) match {
            case ConnectivityState.READY =>
              info(s"Channel for ${nodeAddress} is ready")
            // OK
            case ConnectivityState.SHUTDOWN =>
              throw new IllegalStateException(s"Failed to open a channel for ${nodeAddress}")
            case other =>
              warn(s"Channel state for ${nodeAddress} is ${other}. Sleeping for 100ms")
              Thread.sleep(100)
              loop
          }
        }

        loop
        ServiceGrpc.newSyncClient(channel)
      }
    )
  }

  override def close(): Unit = {
    Control.closeResources(clientHolder.values.toSeq: _*)
  }
}
