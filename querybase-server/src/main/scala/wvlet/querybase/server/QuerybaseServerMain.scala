package wvlet.querybase.server

import wvlet.airframe.http.ServerAddress
import wvlet.airframe.launcher.{Launcher, command, option}
import wvlet.log.{LogSupport, Logger}
import wvlet.querybase.api.BuildInfo
import wvlet.querybase.server.backend.BackendServer.CoordinatorServer
import wvlet.querybase.server.backend.{BackendServer, CoordinatorConfig, WorkerConfig, WorkerService}
import wvlet.querybase.server.frontend.{FrontendServer, FrontendServerConfig}

/** */
object QuerybaseServerMain {
  def main(args: Array[String]): Unit = {
    Logger.init
    Launcher.of[QuerybaseServerMain].execute(args)
  }
}

class QuerybaseServerMain(
    @option(prefix = "-h,--help", description = "Show help messages", isHelp = true) help: Boolean
) extends LogSupport {

  @command(isDefault = true)
  def default = {
    info("Type --help to see the list of commands")
  }

  @command(description = "Start Querybase server")
  def server(
      @option(prefix = "-p,--port", description = "port number (default: 8080)")
      port: Int = 8080
  ): Unit = {
    info(s"Querybase version:${BuildInfo.version}")

    val config = FrontendServerConfig(port = port)
    FrontendServer
      .design(config)
      .build[FrontendServer] { server =>
        server.waitForTermination
      }
  }

  @command(description = "Launch a standalone service")
  def standalone(
      @option(prefix = "-p,--port", description = "port number (default: 8080)")
      port: Int = 8080
  ): Unit = {

    val randomPorts        = BackendServer.randomPort(2)
    val coordinatorAddress = ServerAddress(s"localhost:${randomPorts(0)}")
    val coordinatorConfig  = CoordinatorConfig(serverAddress = coordinatorAddress)
    val workerConfig = WorkerConfig(
      serverAddress = ServerAddress(s"localhost:${randomPorts(1)}"),
      coordinatorAddress = coordinatorAddress
    )
    val frontendServerConfig = FrontendServerConfig(port = port, coordinatorAddress = coordinatorAddress)
    val design = FrontendServer
      .design(frontendServerConfig)
      .add(BackendServer.coordinatorDesign(coordinatorConfig))
      .add(BackendServer.workerDesign(workerConfig))
      .withProductionMode

    design.build[StandaloneService] { service =>
      service.awaitTermination
    }
  }
}

class StandaloneService(
    frontendServer: FrontendServer,
    coordinatorServer: CoordinatorServer,
    workerService: WorkerService
) {

  def awaitTermination: Unit = {
    frontendServer.waitForTermination
  }

}
