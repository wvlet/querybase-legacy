package wvlet.querybase.ui.component.explore

import org.scalajs.dom.Event
import org.scalajs.dom.ext.KeyCode
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.SearchApi.{SearchItem, SearchRequest}
import wvlet.querybase.api.frontend.ServiceJSClient
import wvlet.querybase.ui.RPCQueue
import wvlet.querybase.ui.component.common.{HStack, LabeledForm, VStack}
import wvlet.querybase.ui.component.notebook.NotebookEditor
import wvlet.querybase.ui.component.{ShortcutKeyDef, ShortcutKeys}

/**
  */
class ExploreWindow(notebookEditor: NotebookEditor, serviceJSClient: ServiceJSClient)
    extends RxElement
    with RPCQueue
    with LogSupport {

  private val searchBox = new ExploreSearchBox(serviceJSClient)

  override def render: RxElement = {
    VStack(
      searchBox,
      new TimelineView
    )
  }
}

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

  private val searchForm = LabeledForm()
    .withLabel(i(cls -> "fa fa-search"))
    .withPlaceholder("Search ...")
    .onChange { keyword: String => searchCandidates(keyword) }
    .onEnter { keyword: String =>
      searchItems(keyword)
    }

  private val searchResultList = SearchResultWindow().onSelect { x: SearchItem =>
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
    // TODO Support onDidMount in RxElement
    scalajs.js.timers.setTimeout(10) {
      searchForm.focus
    }

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
