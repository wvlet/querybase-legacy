package wvlet.querybase.ui.component.explore

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.ui.component.{RxRouter, ServiceSelector}
import wvlet.querybase.ui.component.common.{HStack, VStack}

/** */
class ExploreWindow(rxRouter: RxRouter, searchBox: ExploreSearchBox, serviceSelector: ServiceSelector)
    extends RxElement
    with LogSupport {

  private val queryEditor = new QueryEditor()

  override def render: RxElement = {
    scala.scalajs.js.timers.setTimeout(100) {
      searchBox.focus
    }

    div(
      cls -> "my-1",
      VStack(
        searchBox
//        rxRouter.current.transform {
//          case Some(route) =>
//            queryEditor.setText(s"-- ${route.params.mkString(", ")}")
//            queryEditor
//          case None =>
//            queryEditor
//        }
      )
    )
  }
}
