package wvlet.querybase.server.backend

import wvlet.airframe.newDesign
import wvlet.querybase.server.backend.BackendServer.WorkerServer
import wvlet.querybase.server.backend.WorkerService.BackgroundExecutor

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

/**
  */
class WorkerService(
    workerConfig: WorkerConfig,
    workerServer: WorkerServer,
    rpcClientProvider: RPCClientProvider,
    executor: BackgroundExecutor
) {

  private val self                   = workerConfig.toNode
  private lazy val coordinatorClient = rpcClientProvider.getSyncClientFor(workerConfig.coordinatorAddress.toString())

  // Polling coordinator every 5 seconds
  executor.scheduleAtFixedRate(
    new Runnable {
      override def run(): Unit = {
        coordinatorClient.v1.CoordinatorApi.register(self)
      }
    },
    0,
    5,
    TimeUnit.SECONDS
  )

}

object WorkerService {

  type BackgroundExecutor = ScheduledExecutorService

  def design = newDesign
    .bind[BackgroundExecutor].toInstance(
      Executors.newSingleThreadScheduledExecutor()
    )
    .onShutdown(_.shutdownNow())

}
