package wvlet.querybase.ui

import org.scalajs.dom
import wvlet.airframe.Design
import wvlet.airframe.rx.html.DOMRenderer
import wvlet.airframe.rx.html.widget.auth.GoogleAuthConfig
import wvlet.airframe.surface.Surface
import wvlet.log.{LogLevel, LogSupport, Logger}
import wvlet.querybase.ui.component.page.{HomePage, MainPage, NotebookPage}
import wvlet.querybase.ui.component.{RxRoute, RxRouter}

import scala.scalajs.js.annotation.JSExport

/**
  */
object QuerybaseUI extends LogSupport {
  private def router = new RxRouter(
    prefix = "/ui/#",
    routes = Seq(
      //RxRoute(path = "/home", title = "Home - Querybase", Surface.of[HomePage]),
      RxRoute(path = "/explore", title = "Explore - Querybase", Surface.of[NotebookPage]),
      //RxRoute(path = "/services", title = "Services - Querybase", Surface.of[ServiceCatalog]),
      RxRoute(path = "*", title = "Home - Querybase", Surface.of[HomePage])
    )
  )

  def design: Design = {
    Design.newDesign
      .bind[GoogleAuthConfig].toInstance(
        GoogleAuthConfig(
          clientId = "793299428025-n6kmmrmcs4g80kibc7m7qakn6vc656bt.apps.googleusercontent.com"
        )
      )
      .bind[RxRouter].toInstance(router)
  }

  @JSExport
  def main(args: Array[String]): Unit = {
    Logger.setDefaultLogLevel(LogLevel.INFO)
    Logger("wvlet.querybase.ui").setLogLevel(LogLevel.DEBUG)
    //Logger("wvlet.airframe.http").setLogLevel(LogLevel.DEBUG)
    info(s"Started")

    initializeUI
  }

  def initializeUI: Unit = {

    // Insert main node if not exists
    val mainNode = dom.document.getElementById("main") match {
      case null =>
        val elem = dom.document.createElement("div")
        elem.setAttribute("id", "main")
        dom.document.body.appendChild(elem)
      case other => other
    }

    val session = design.noLifeCycleLogging.newSession
    val panel   = session.build[MainPage]
    session.start
    DOMRenderer.renderTo(mainNode, panel)
  }
}
