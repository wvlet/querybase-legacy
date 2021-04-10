package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi
import wvlet.querybase.api.backend.v1.WorkerApi.QueryExecutionInfo
import wvlet.querybase.server.backend.WorkerConfig
import wvlet.querybase.server.backend.query.QueryExecutor
import wvlet.querybase.server.backend.query.QueryExecutor.QueryExecutionRequest

class WorkerApiImpl(queryExecutor: QueryExecutor, workerConfig: WorkerConfig) extends WorkerApi with LogSupport {
  override def runTrinoQuery(
      queryId: String,
      service: WorkerApi.TrinoService,
      query: String,
      schema: String
  ): QueryExecutionInfo = {
    queryExecutor.executeQuery(
      QueryExecutionRequest(queryId = queryId, query = query, schema = schema, service = service)
    )
    QueryExecutionInfo(queryId, nodeId = workerConfig.name)
  }
}
