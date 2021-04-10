package wvlet.querybase.server.backend

import wvlet.airframe.{Design, newDesign}
import wvlet.querybase.api.backend.v1.CoordinatorApi.Node
import wvlet.querybase.server.backend.BackendServer.WorkerServer
import wvlet.querybase.server.backend.WorkerService.WorkerBackgroundExecutor

import java.net.InetAddress
import java.time.Instant
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

/**
  */
class WorkerService(
    workerConfig: WorkerConfig,
    // Adding this dependency to start WorkerServer
    workerServer: WorkerServer,
    rpcClientProvider: RPCClientProvider,
    executor: WorkerBackgroundExecutor
) {

  private val self: Node = {
    val localHost = InetAddress.getLocalHost
    val localAddr = s"${localHost.getHostAddress}:${workerConfig.serverAddress.port}"
    Node(name = workerConfig.name, address = localAddr, isCoordinator = false, startedAt = Instant.now())
  }

  private lazy val coordinatorClient = rpcClientProvider.getSyncClientFor(workerConfig.coordinatorAddress.toString())

  // Polling coordinator every 5 seconds
  executor.scheduleAtFixedRate(
    () => { coordinatorClient.v1.CoordinatorApi.register(self) },
    0,
    5,
    TimeUnit.SECONDS
  )

}

object WorkerService {

  type WorkerBackgroundExecutor = ScheduledExecutorService

  def design: Design = newDesign
    .bind[WorkerBackgroundExecutor].toInstance(
      Executors.newSingleThreadScheduledExecutor()
    )
    .onShutdown(_.shutdownNow())

}
