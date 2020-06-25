package wvlet.querybase.server.api

import wvlet.airframe._
import wvlet.querybase.api.v1.query.QueryLogApi
import wvlet.querybase.api.v1.query.QueryLogApi._
import wvlet.querybase.store.{QueryList, QueryStorage}

/**
  */
trait QueryLogApiImpl extends QueryLogApi {
  private val storage = bind[QueryStorage]

  override def addQueryLog(request: AddQueryLogRequest): AddQueryLogResponse = {
    storage.add(QueryList(request.logs))
    AddQueryLogResponse(request.uuid)
  }

  override def addPrestoQueryStats(request: AddPrestoQueryStatsRequest): AddPrestoQueryStatsResponse = {
    AddPrestoQueryStatsResponse(request.uuid)
  }

  override def addPrestoQueryStageStats(request: AddPrestoQueryStageStatsRequest): AddPrestoQueryStageStatsResponse = {
    AddPrestoQueryStageStatsResponse(request.uuid)
  }
}
