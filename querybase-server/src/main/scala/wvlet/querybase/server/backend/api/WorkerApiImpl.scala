package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi
import wvlet.querybase.api.backend.v1.WorkerApi.TaskInfo

class WorkerApiImpl extends WorkerApi with LogSupport {
  //  override def runTask(taskRequest: WorkerApi.QueryTaskRequest): WorkerApi.TaskInfo = {
  //    info(s"Run task: ${taskRequest}")
  //    TaskInfo(taskRequest.queryId, taskId = s"${taskRequest.queryId}.0")
  //  }
  override def runTask(queryId: String, service: WorkerApi.TrinoService, query: String): TaskInfo = {
    info(s"Run task for ${queryId}, service: ${service}")
    TaskInfo(queryId, taskId = s"${queryId}.0")
  }
}
