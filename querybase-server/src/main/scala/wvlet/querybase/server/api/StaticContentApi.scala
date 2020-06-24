package wvlet.querybase.server.api

import java.io.File

import wvlet.airframe.http.{Endpoint, StaticContent}
import wvlet.log.LogSupport
import wvlet.querybase.api.BuildInfo

/**
  * API for hosting static contents
  */
class StaticContentApi extends LogSupport {
  private val webResourceDir = new File(sys.props.getOrElse("prog.home", "."), "public").getPath

  private lazy val baseDir = {
    val currentDir = new File("").getName
    currentDir match {
      case "querybase-server" => ".."
      case _                  => "."
    }
  }

  private val staticContent = StaticContent
    .fromDirectory(webResourceDir)
    .fromDirectory(s"${baseDir}/querybase-ui/src/main/public")
    .fromDirectory(s"${baseDir}/querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}")
    .fromDirectory(s"${baseDir}/querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}/scalajs-bundler/main")

  @Endpoint(path = "/ui/*path")
  def ui(path: String) = {
    staticContent(if (path.isEmpty) "index.html" else path)
  }
}
