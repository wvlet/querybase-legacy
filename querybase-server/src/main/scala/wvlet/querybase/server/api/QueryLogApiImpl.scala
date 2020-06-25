package wvlet.querybase.server.api

import wvlet.querybase.api.v1.query.QueryLogApi
import wvlet.airframe._
import wvlet.querybase.api.v1.query.QueryLogApi.AddQueryLogResponse
import wvlet.querybase.store.{QueryList, QueryStorage}

/**
  */
trait QueryLogApiImpl extends QueryLogApi {
  private val storage = bind[QueryStorage]

  override def add(request: QueryLogApi.AddQueryLogRequest): QueryLogApi.AddQueryLogResponse = {
    storage.add(QueryList(request.logs))
    AddQueryLogResponse(request.uuid)
  }
}
