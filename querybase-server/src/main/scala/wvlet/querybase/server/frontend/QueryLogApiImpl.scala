package wvlet.querybase.server.frontend

import wvlet.airframe.bind
import wvlet.querybase.api.backend.v1.query.QueryLogApi
import wvlet.querybase.api.backend.v1.query.QueryLogApi._
import wvlet.querybase.store.{QueryList, QueryStorage}

/**
  */
trait QueryLogApiImpl extends QueryLogApi {
  private val storage = bind[QueryStorage]

  override def addQueryLog(request: AddQueryLogRequest): AddQueryLogResponse = {
    storage.add(QueryList(request.logs))
    AddQueryLogResponse(request.uuid)
  }

  override def addTableScanLog(request: AddTableScanLogRequest): AddTableScanLogResponse = {
    AddTableScanLogResponse(request.uuid)
  }

  override def addPrestoQueryStats(request: AddPrestoQueryStatsRequest): AddPrestoQueryStatsResponse = {
    AddPrestoQueryStatsResponse(request.uuid)
  }

  override def addPrestoQueryStageStats(request: AddPrestoQueryStageStatsRequest): AddPrestoQueryStageStatsResponse = {
    AddPrestoQueryStageStatsResponse(request.uuid)
  }
}
