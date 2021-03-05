package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi
import wvlet.querybase.api.backend.v1.WorkerApi.TaskInfo
import wvlet.querybase.server.backend.query.TaskManager

class WorkerApiImpl(taskManager: TaskManager) extends WorkerApi with LogSupport {
  //  override def runTask(taskRequest: WorkerApi.QueryTaskRequest): WorkerApi.TaskInfo = {
  //    info(s"Run task: ${taskRequest}")
  //    TaskInfo(taskRequest.queryId, taskId = s"${taskRequest.queryId}.0")
  //  }
  override def runTrinoTask(queryId: String, service: WorkerApi.TrinoService, query: String): TaskInfo = {
    info(s"Run task for ${queryId}, service: ${service}")
    TaskInfo(queryId, taskId = s"${queryId}.0")
  }
}
