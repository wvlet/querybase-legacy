package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC

import java.time.Instant

/**
  */
@RPC
trait WorkerApi {
  import WorkerApi._

  def runTask(taskRequest: TaskRequest): TaskInfo

}

object WorkerApi {

  case class TaskRequest(queryId: String)
  case class TaskInfo(queryId: String, taskId: String, createdAt: Instant = Instant.now())

}
