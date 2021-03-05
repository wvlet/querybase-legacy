package wvlet.querybase.ui.component.page

import wvlet.airframe._
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.frontend.FrontendApi.SubmitQueryRequest
import wvlet.querybase.ui.RPCService
import wvlet.querybase.ui.component.{QueryListPanel, ServiceSelector, Table}
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
trait ExplorePage extends RxElement {
  private val rpcService     = bind[RPCService]
  private val queryListPanel = bind[QueryListPanel]

  private val queryEditor = new QueryEditor("select 1", rpcService)

  override def render: RxElement = {
    div(
      cls -> "w-100 h-100",
      div(
        cls -> "d-flex flex-column",
        div(
          cls -> "mb-auto",
          queryEditor
        ),
        hr(),
        div(
          queryListPanel
        )
      )
    )
  }
}

class QueryEditor(query: String, rpcService: RPCService) extends RxElement with LogSupport {
  private val serviceSelector = new ServiceSelector(Seq.empty)
  private val textEditor      = new TextEditor(query, onEnter = submitQuery(_))

  override def render: RxElement = {
    div(
      rpcService.rpcRx(_.FrontendApi.serviceCatalog()).map { lst =>
        serviceSelector.updateList(lst)
        serviceSelector
      },
      textEditor
    )
  }

  private implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private def submitQuery(query: String): Unit = {
    val selectedService = serviceSelector.selectedService
    info(s"Submit to ${selectedService.name}: ${query}")
    rpcService
      .rpc(_.FrontendApi.submitQuery(SubmitQueryRequest(query = query, serviceName = selectedService.name))).map {
        resp =>
          info(s"query_id: ${resp.queryId}")
      }
  }
}