package wvlet.querybase.api.frontend

import wvlet.airframe.http.RPC
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo

import java.util.UUID

@RPC
trait QueryApi {
  import QueryApi._
  def submitQuery(request: SubmitQueryRequest): SubmitQueryResponse

  def listQueries(): Seq[QueryInfo]
}

object QueryApi {
  case class SubmitQueryRequest(query: String, uuid: UUID = UUID.randomUUID())
  case class SubmitQueryResponse(queryId: String)
}
