package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryId

import java.time.Instant

/**
  */
@RPC
trait WorkerApi {
  import WorkerApi._

  def runTrinoTask(trinoTaskRequest: TrinoTaskRequest): TrinoTaskInfo
}

object WorkerApi {

  case class TrinoTaskRequest(queryId: QueryId, query: String)
  case class TrinoService(address: String, connector: String, schema: String, user: String)
  case class TrinoTaskInfo(queryId: QueryId, taskId: String, createdAt: Instant = Instant.now())

}
