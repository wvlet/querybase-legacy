package wvlet.querybase.ui

import org.scalajs.dom
import wvlet.airframe.Design
import wvlet.airframe.http.js.JSHttpClient
import wvlet.airframe.rx.RxOptionVar
import wvlet.airframe.rx.html.DOMRenderer
import wvlet.airframe.rx.html.widget.auth.{GoogleAuth, GoogleAuthConfig, GoogleAuthProfile}
import wvlet.airframe.surface.Surface
import wvlet.log.{LogLevel, LogSupport, Logger}
import wvlet.querybase.api.frontend.code.NotebookApi.Notebook
import wvlet.querybase.ui.component.page.{
  ExplorePage,
  HomePage,
  JobsPage,
  MainPage,
  NotebookPage,
  ServicePage,
  SystemPage,
  TestPage
}
import wvlet.querybase.ui.component.{QueryListPanel, RxRoute, RxRouter}

import scala.scalajs.js.annotation.JSExport

/**
  */
object QuerybaseUI extends LogSupport {
  private def router = new RxRouter(
    prefix = "/ui/#",
    routes = Seq(
      //RxRoute(path = "/home", title = "Home - Querybase", Surface.of[HomePage]),
      RxRoute(
        path = "/explore",
        title = "Explore - Querybase",
        Surface.of[ExplorePage],
        children = Seq(
          RxRoute(path = "/:page_id", title = "Explore - Querybase", Surface.of[ExplorePage])
        )
      ),
      RxRoute(path = "/notebook", title = "Notebook - Querybase", Surface.of[NotebookPage]),
      RxRoute(path = "/jobs", title = "Jobs - Querybase", Surface.of[JobsPage]),
      RxRoute(path = "/services", title = "Services - Querybase", Surface.of[ServicePage]),
      RxRoute(path = "/system", title = "System - Querybase", Surface.of[SystemPage]),
      RxRoute(path = "/test", title = "Test", Surface.of[TestPage]),
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
      .bind[HomePage].toSingleton
      .bind[ExplorePage].toSingleton
      .bind[SystemPage].toSingleton
      .bind[ServicePage].toSingleton
      .bind[RPCService].toSingleton
      .bind[QueryListPanel].toSingleton
      .bind[TestPage].toSingleton
      .bind[JSHttpClient].toProvider { auth: GoogleAuth =>
        JSHttpClient.defaultClient.withConfig { config =>
          config.withRequestFilter { req =>
            auth.getCurrentUser match {
              case Some(user) =>
                req.withHeader("Authorization", s"Bearer ${user.id_token}")
              case _ =>
                req
            }
          }
        }
      }

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
    try {
      DOMRenderer.renderTo(mainNode, panel)
    } catch {
      case e: Throwable =>
        warn(e)
    }
  }
}
