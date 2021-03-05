package wvlet.querybase.ui.component

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.ui.RPCService

trait QueryListPanel extends RxElement with RPCService {
  private val queryList = Rx.variable(Seq.empty[QueryInfo])

  private def renderStatus(s: QueryStatus): RxElement = {
    val color = s match {
      case QueryStatus.RUNNING  => "success"
      case QueryStatus.FINISHED => "info"
      case QueryStatus.FAILED   => "danger"
      case QueryStatus.CANCELED => "warning"
      case QueryStatus.QUEUED   => "secondary"
      case _                    => "light"
    }
    span(cls -> s"badge badge-${color}", s.toString)
  }

  override def render: RxElement = {
    new Table(Seq("query_id", "service", "type", "status", "elapsed", "query"))(
      style -> "max-height: 200px;",
      queryList.map { ql =>
        ql.map { q =>
          tr(
            td(cls -> "text-monospace", small(q.queryId)),
            td(q.serviceName),
            td(q.serviceType),
            td(
              renderStatus(q.queryStatus)
            ),
            td(cls -> "text-center", q.elapsed.toString()),
            td(q.query)
          )
        }
      },
      repeatRpc(1500)(_.FrontendApi.listQueries()).map { lst =>
        queryList.forceSet(lst)
        span()
      }
    )
  }
}
