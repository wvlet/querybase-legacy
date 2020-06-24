package wvlet.querybase.server

import wvlet.airframe.launcher.{Launcher, command, option}
import wvlet.log.Logger

/**
  */
object QuerybaseServerMain {
  def main(args: Array[String]): Unit = {
    Logger.init
    Launcher.of[QuerybaseServerMain].execute(args)
  }
}

class QuerybaseServerMain(
    @option(prefix = "-h,--help", description = "Show help messages", isHelp = true) help: Boolean
) {
  @command(description = "Start Querybase server")
  def server(
      @option(prefix = "-p,--port", description = "port number (default: 8080)")
      port: Int = 8080
  ): Unit = {

    val config = QuerybaseServerConfig(port = port)
    QuerybaseServer
      .design(config)
      .build[QuerybaseServer] { server =>
        server.waitForTermination
      }
  }
}
