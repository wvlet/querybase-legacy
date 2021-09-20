package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.airframe.surface.secret
import wvlet.querybase.api.backend.v1.CoordinatorApi.NodeId

import java.time.Instant

/** */
@RPC
trait WorkerApi {
  import WorkerApi._

  def runTrinoQuery(
      queryId: String,
      service: TrinoService,
      query: String,
      schema: String,
      limit: Option[Int],
      taskId: Option[String]
  ): QueryExecutionInfo
}

object WorkerApi {
  case class TrinoService(address: String, connector: String, @secret user: String)
  case class QueryExecutionInfo(queryId: String, nodeId: NodeId, createdAt: Instant = Instant.now())

}
