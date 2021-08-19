package wvlet.querybase.ui.component.explore

import org.scalajs.dom.{Event, KeyboardEvent}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, i, onblur, style}
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.SearchApi.{SearchItem, SearchRequest}
import wvlet.querybase.api.frontend.ServiceJSClient
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.common.{LabeledForm, VStack}
import wvlet.querybase.ui.component.{RichEvent, ShortcutKeyDef, ShortcutKeys}

/** */
class ExploreSearchBox(serviceJSClient: ServiceJSClient) extends RxElement with RPCQueue with LogSupport {
  private def shortcutKeys = new ShortcutKeys(
    Seq(
      ShortcutKeyDef(
        // '/'
        keyCode = 191,
        description = "Enter search box",
        handler = { e: Event =>
          searchForm.focus
        }
      ),
      ShortcutKeyDef(
        keyCode = KeyCode.Escape,
        description = "Exit from search",
        handler = { e: Event =>
          exitSearch
        }
      )
    )
  )

  private val searchForm: LabeledForm = LabeledForm()
    .withLabel(i(cls -> "fa fa-search"))
    .withPlaceholder("Search ...")
    .withSmallSize
    .onChange { keyword: String => searchCandidates(keyword) }
    .onBlur { () =>
      if (!searchResultList.hasFocus) {
        exitSearch
      }
    }
    .onEnter { keyword: String =>
      searchItems(keyword)
    }
    .onKeyEvent { (e: KeyboardEvent) =>
      if (e.keyCode == KeyCode.Down) {}
    }

  private lazy val searchResultList = SearchResultWindow().onSelect { x: SearchItem =>
    info(s"Selected :${x}")
    searchForm.setText(x.title)
  }

  private def searchItems(keyword: String): Unit = {
    info(s"Search: ${keyword}")
    // do nothing for now
  }

  private def searchCandidates(keyword: String): Unit = {
    serviceJSClient.FrontendApi
      .search(SearchRequest(keyword = keyword)).foreach { resp =>
        searchResultList.setList(resp.results)
      }
  }

  private def exitSearch: Unit = {
    searchResultList.hide
    searchForm.blur
  }

  override def render: RxElement = {
//    // TODO Support onDidMount in RxElement
//    scalajs.js.timers.setTimeout(10) {
//      searchForm.focus
//    }

    div(
      shortcutKeys,
      VStack(
        div(
          style -> "width: 500px;",
          searchForm
        ),
        searchResultList
      )
    )
  }
}
