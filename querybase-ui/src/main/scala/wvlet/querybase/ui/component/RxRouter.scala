package wvlet.querybase.ui.component

/**
 *
 */
import org.scalajs.dom
import org.scalajs.dom.PopStateEvent
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.rx.{Rx, RxOption, RxOptionVar}
import wvlet.airframe.surface.Surface
import wvlet.log.LogSupport

import scala.annotation.tailrec

/**
 */
class RxRouter(prefix: String, routes: Seq[RxRoute]) extends LogSupport {
  private lazy val currentRoute: RxOptionVar[RouteMatch] = Rx.optionVariable(None)

  def current: RxOption[RouteMatch] = currentRoute

  def setPath(path: String, updateHistory: Boolean = true): Unit = {
    val subpath                          = path.stripPrefix(prefix)
    val matchedRoute: Option[RouteMatch] = routes.flatMap(_.matches(subpath)).headOption
    matchedRoute match {
      case Some(m) =>
        if (updateHistory) {
          pushState(m)
        }
        currentRoute := Some(m)
      case None =>
        throw new IllegalArgumentException(s"No matching route for ${path}")
    }
  }

  // Set the default path based on the browser location
  {
    val initPath = dom.window.location.hash.stripPrefix("#")
    info(s"initial path: ${initPath}")
    setPath(initPath, updateHistory = false)
  }

  // Monitor onpopstate event for supporting browser-back/forward buttons
  dom.window.onpopstate = { e: PopStateEvent =>
    try {
      Option(e.state).foreach { s =>
        val state = MessageCodec.of[Map[String, Any]].fromJson(s.toString)
        state.get("location").collect { case location: String =>
          debug(s"Browser history popped: ${location}")
          setPath(location, updateHistory = false)
        }
      }
    } catch {
      case e: Throwable =>
        warn(e)
    }
  }

  private def pushState(m: RouteMatch): Unit = {
    // Record the page transition to the browser history so that we can use back/forward button of the browser
    val url      = s"${prefix}${m.path}"
    val newTitle = m.route.title
    debug(s"Push history state: ${url}")
    dom.window.history.pushState(s"""{"location":"${url}"}""", newTitle, url)
    dom.document.title = newTitle
  }
}

case class RouteMatch(path: String, route: RxRoute, params: Map[String, String])

case class RxRoute(
        // path like '/home', '/user/:user_id'
        path: String,
        title: String,
        // Create RxElement from a given PageTarget
        pageSurface: Surface,
        children: Seq[RxRoute] = Seq.empty,
        redirect: Option[RxRoute] = None
) {
  private val pathComponents = path.split("/")

  def matches(inputPath: String): Option[RouteMatch] = {
    val pc = inputPath.split("/")
    if (pc.length < pathComponents.length) {
      None
    } else {
      @tailrec
      def loop(i: Int, params: Map[String, String]): Option[RouteMatch] = {
        if (i == pathComponents.length) {
          Some(RouteMatch(inputPath, this, params))
        } else {
          val expected = pathComponents(i)
          if (expected.startsWith(":")) {
            loop(i + 1, params + (expected.substring(1) -> pc(i)))
          } else if (expected.startsWith("*")) {
            loop(i + 1, params)
          } else if (expected == pc(i)) {
            loop(i + 1, params)
          } else {
            None
          }
        }
      }
      // TODO Find a match from children
      loop(0, Map.empty)
    }
  }
}
