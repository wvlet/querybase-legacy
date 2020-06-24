package wvlet.querybase.server

import wvlet.airframe.launcher.{Launcher, command, option}
import wvlet.log.{LogSupport, Logger}
import wvlet.querybase.api.BuildInfo

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
) extends LogSupport {
  @command(description = "Start Querybase server")
  def server(
      @option(prefix = "-p,--port", description = "port number (default: 8080)")
      port: Int = 8080
  ): Unit = {
    info(s"Querybase version:${BuildInfo.version}")

    val config = QuerybaseServerConfig(port = port)
    QuerybaseServer
      .design(config)
      .build[QuerybaseServer] { server =>
        server.waitForTermination
      }
  }
}
