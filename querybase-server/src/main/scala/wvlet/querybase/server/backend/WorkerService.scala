package wvlet.querybase.server.backend

import wvlet.airframe.{bindLocal, newDesign}
import wvlet.querybase.server.backend.BackendServer.WorkerServer
import wvlet.querybase.server.backend.WorkerService.BackgroundExecutor

import java.util.concurrent.{Executor, ExecutorService, Executors}
import javax.annotation.PostConstruct

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

  executor.submit(new Runnable {
    override def run(): Unit = {
      coordinatorClient.v1.CoordinatorApi.register(self)
    }
  })

}

object WorkerService {

  type BackgroundExecutor = ExecutorService

  def design = newDesign
    .bind[BackgroundExecutor].toInstance(
      Executors.newCachedThreadPool()
    )
    .onShutdown(_.shutdownNow())

}
