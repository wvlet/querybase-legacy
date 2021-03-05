package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoTaskInfo

class WorkerApiImpl extends WorkerApi with LogSupport {
  override def runTrinoTask(taskRequest: WorkerApi.TrinoTaskRequest): WorkerApi.TrinoTaskInfo = {
    info(s"Run task: ${taskRequest}")
    TrinoTaskInfo(taskRequest.queryId, taskId = s"${taskRequest.queryId}.0")
  }
}
