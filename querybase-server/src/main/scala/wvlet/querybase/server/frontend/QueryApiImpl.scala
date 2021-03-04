package wvlet.querybase.server.frontend

import wvlet.querybase.api.backend.v1.CoordinatorApi
import wvlet.querybase.api.backend.v1.CoordinatorApi.NewQueryRequest
import wvlet.querybase.api.frontend.QueryApi
import wvlet.querybase.api.frontend.QueryApi.SubmitQueryResponse
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

class QueryApiImpl(coordinatorClient: CoordinatorClient) extends QueryApi {

  override def submitQuery(request: QueryApi.SubmitQueryRequest): QueryApi.SubmitQueryResponse = {
    val r = coordinatorClient.v1.CoordinatorApi.newQuery(NewQueryRequest(request.query))
    SubmitQueryResponse(r.queryId)
  }

  override def listQueries(): Seq[CoordinatorApi.QueryInfo] = {
    coordinatorClient.v1.CoordinatorApi.listQueries()
  }
}
