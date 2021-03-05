package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi
import wvlet.querybase.api.backend.v1.WorkerApi.QueryExecutionInfo
import wvlet.querybase.server.backend.WorkerConfig
import wvlet.querybase.server.backend.query.TaskManager

class WorkerApiImpl(taskManager: TaskManager, workerConfig: WorkerConfig) extends WorkerApi with LogSupport {
  override def runTrinoQuery(queryId: String, service: WorkerApi.TrinoService, query: String): QueryExecutionInfo = {
    info(s"Run Trino query for ${queryId}")
    QueryExecutionInfo(queryId, nodeId = workerConfig.name)
  }
}
