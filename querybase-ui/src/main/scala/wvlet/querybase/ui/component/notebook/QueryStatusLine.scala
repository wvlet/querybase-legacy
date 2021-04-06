package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{QueryInfo, QueryResult}
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.ui.component.QueryListPanel

/**
  */
class QueryStatusLine(queryInfo: Option[QueryInfo]) extends RxElement with LogSupport {

  private def status(s: QueryStatus): RxElement = {
    QueryListPanel.renderStatus(s)(cls += "mr-2")
  }

  private def queryStatusLine(qi: QueryInfo): RxElement = {
    div(
      table(
        tr(
          td(
            small(
              status(qi.queryStatus),
              span(
                s"[${qi.serviceType}:${qi.serviceName}] ${qi.queryId}: ${qi.elapsed}"
              )
            )
          )
        ),
        qi.error.map { err =>
          tr(
            td(
              small(s"${err.errorMessage}")
            )
          )
        }
      )
    )
  }

  override def render: RxElement = queryInfo match {
    case Some(qi) if qi.result.nonEmpty =>
      div(
        queryStatusLine(qi),
        new QueryResultViewer(qi.result.get)
      )
    case Some(qi) =>
      queryStatusLine(qi)
    case None =>
      small(
        status(QueryStatus.STARTING),
        "running..."
      )
  }
}
