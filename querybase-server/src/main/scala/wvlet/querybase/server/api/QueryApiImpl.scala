package wvlet.querybase.server.api

import wvlet.querybase.api.v1.query.QueryApi

/**
  */
class QueryApiImpl extends QueryApi {
  override def list: Seq[QueryApi.Query] = Seq.empty
}
