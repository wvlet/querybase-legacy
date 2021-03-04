package wvlet.querybase.ui.component.page

import wvlet.airframe._
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.QueryApi.SubmitQueryRequest
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.Table
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.querybase.ui.component.notebook.NotebookFrame

/**
  */
trait ExplorePage extends RxElement {
  private val rpcService     = bind[RPCService]
  private val queryListPanel = bind[QueryListPanel]

  private val queryEditor = new QueryEditor("select 1", rpcService)

  override def render: RxElement = div(
    cls -> "w-100",
    queryEditor,
    queryListPanel
  )
}

class QueryEditor(query: String, rpcService: RPCService) extends RxElement with LogSupport {
  private val textEditor = new TextEditor(query, onEnter = submitQuery(_))

  override def render: RxElement = {
    textEditor
  }

  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private def submitQuery(query: String): Unit = {
    info(s"Submit query:\n${query}")
    rpcService.rpc(_.QueryApi.submitQuery(SubmitQueryRequest(query = query))).map { resp =>
      info(s"query_id: ${resp.queryId}")
    }
  }
}

trait QueryListPanel extends RxElement with RPCService {
  private val queryList = Rx.variable(Seq.empty[QueryInfo])

  override def render: RxElement = {
    new Table(Seq("query_id", "status", "query"))(
      tbody(
        queryList.map { ql =>
          ql.map { q =>
            tr(
              td(q.queryId),
              td(q.queryStatus.toString),
              td(q.query)
            )
          }
        },
        repeatRpc(1000)(_.QueryApi.listQueries()).map { lst =>
          queryList := lst
          span()
        }
      )
    )
  }
}
