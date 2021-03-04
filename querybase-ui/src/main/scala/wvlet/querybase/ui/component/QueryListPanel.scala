package wvlet.querybase.ui.component

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.ui.RPCService

trait QueryListPanel extends RxElement with RPCService {
  private val queryList = Rx.variable(Seq.empty[QueryInfo])

  override def render: RxElement = {
    new Table(Seq("query_id", "service", "type", "status", "elapsed", "query"))(
      tbody(
        queryList.map { ql =>
          ql.map { q =>
            tr(
              td(cls -> "text-monospace", small(q.queryId)),
              td(q.serviceName),
              td(q.serviceType),
              td(q.queryStatus.toString),
              td(q.elapsed.toString()),
              td(q.query)
            )
          }
        },
        repeatRpc(1000)(_.FrontendApi.listQueries()).map { lst =>
          queryList.forceSet(lst)
          span()
        }
      )
    )
  }
}
