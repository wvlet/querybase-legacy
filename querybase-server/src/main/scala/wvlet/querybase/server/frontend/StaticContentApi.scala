package wvlet.querybase.server.frontend

import wvlet.airframe.http.{Endpoint, Http, StaticContent}
import wvlet.log.LogSupport
import wvlet.querybase.api.BuildInfo

/** API for hosting static contents
  */
class StaticContentApi extends LogSupport {

  private val staticContent = {
    // prog.home JVM system property will be provided by the sbt-pack launcher script
    sys.props.get("prog.home") match {
      case Some(webResourceDir) =>
        // For production server
        StaticContent
          .fromDirectory(webResourceDir)
      case _ =>
        // For testing
        val baseDir = "."
        StaticContent
          .fromDirectory(s"${baseDir}/querybase-ui/src/main/public")
          .fromDirectory(s"${baseDir}/querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}")
          .fromDirectory(s"${baseDir}/querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}/scalajs-bundler/main")
    }
  }

  @Endpoint(path = "/")
  def index: Unit = {
    throw Http.redirectException("/ui/")
  }

  @Endpoint(path = "/ui/*path")
  def ui(path: String) = {
    staticContent(if (path.isEmpty) {
      "index.html"
    } else {
      path
    })
  }
}
