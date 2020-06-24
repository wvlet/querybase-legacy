package wvlet.querybase.ui

import wvlet.airframe.Design
import wvlet.log.{LogLevel, LogSupport, Logger}

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import wvlet.airframe.http.rx.html.DOMRenderer
import wvlet.querybase.ui.component.MainPanel

/**
  */
object QuerybaseUI extends LogSupport {
  def design = Design.newDesign

  @JSExport
  def main(args: Array[String]): Unit = {
    Logger.setDefaultLogLevel(LogLevel.INFO)
    Logger("wvlet.querybase.ui").setLogLevel(LogLevel.DEBUG)
    //Logger("wvlet.airframe.http").setLogLevel(LogLevel.DEBUG)

    debug(s"started")
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
    val panel   = session.build[MainPanel]
    DOMRenderer.renderTo(mainNode, panel)
    session.start
  }
}
