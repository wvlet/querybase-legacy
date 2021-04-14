package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.ui.component.QueryListPanel

/**
  */
class QueryStatusLine(queryInfo: QueryInfo) extends RxElement with LogSupport {

  private def status(s: QueryStatus): RxElement = {
    QueryListPanel.renderStatus(s)(cls += "mr-2")
  }

  override def render: RxElement = {
    div(
      table(
        tr(
          td(
            small(
              status(queryInfo.queryStatus),
              span(
                s"[${queryInfo.serviceType}:${queryInfo.serviceName}] ${queryInfo.queryId}: ${queryInfo.elapsed}"
              )
            )
          )
        ),
        queryInfo.error.map { err =>
          tr(
            td(
              small(s"${err.errorMessage}")
            )
          )
        }
      )
    )
  }

}
