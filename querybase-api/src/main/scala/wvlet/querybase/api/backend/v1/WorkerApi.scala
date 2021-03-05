package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.airframe.surface.secret

import java.time.Instant

/**
  */
@RPC
trait WorkerApi {
  import WorkerApi._

  def runTrinoTask(queryId: String, service: TrinoService, query: String): TaskInfo
}

object WorkerApi {
  case class TrinoService(address: String, connector: String, schema: String, @secret user: String)
  case class TaskInfo(queryId: String, taskId: String, createdAt: Instant = Instant.now())

}
