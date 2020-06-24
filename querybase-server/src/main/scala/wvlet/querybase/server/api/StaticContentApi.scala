package wvlet.querybase.server.api

import wvlet.airframe.http.{Endpoint, StaticContent}
import wvlet.querybase.api.BuildInfo

/**
  * API for hosting static contents
  */
class StaticContentApi {

  private val staticContent = StaticContent
    .fromDirectory("../querybase-ui/src/main/public")
    // TODO Need a more reliable way to select Scala version
    .fromDirectory(s"../querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}")
    .fromDirectory(s"../querybase-ui/target/scala-${BuildInfo.scalaBinaryVersion}/scalajs-bundler/main")

  @Endpoint(path = "/ui/*path")
  def ui(path: String) = {
    staticContent(if (path.isEmpty) "index.html" else path)
  }
}
