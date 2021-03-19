package wvlet.querybase.ui.component

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.ui.RPCService

import java.util.concurrent.TimeUnit

trait QueryListPanel extends RxElement with RPCService {
  private val queryList =
    repeatRpc(1500, TimeUnit.MILLISECONDS)(_.FrontendApi.listQueries()).cache

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

  private def sortQueryList(ql: Seq[QueryInfo]): Seq[QueryInfo] = {
    ql.sortBy { q =>
      (
        q.queryStatus.isFinished,
        q.completedAt.map(-_.toEpochMilli).getOrElse(-q.elapsed.toMillis.toLong)
      )
    }
  }

  override def render: RxElement = {
    new Table(Seq("query_id", "service", "type", "status", "elapsed", "query"))(
      queryList.map { ql =>
        sortQueryList(ql)
          .map { q =>
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
      }
    )
  }
}
