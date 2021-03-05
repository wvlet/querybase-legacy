package wvlet.querybase.server.backend.query

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryId
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.BackendServer.{CoordinatorClient, coordinatorDesign}
import wvlet.querybase.server.backend.ThreadManager
import wvlet.querybase.server.backend.query.QueryExecutor.{QueryExecutionRequest, QueryExecutorThreadManager}

class QueryExecutor(threadManager: QueryExecutorThreadManager, coordinatorClient: CoordinatorClient)
    extends LogSupport {

  def executeQuery(request: QueryExecutionRequest): Unit = {
    threadManager.submit(execute(request))
  }

  private def execute(request: QueryExecutionRequest): Unit = {
    info(s"Starting query: ${request.queryId}")
    coordinatorClient.v1.CoordinatorApi.updateQueryStatus(request.queryId, QueryStatus.RUNNING)
  }
}

object QueryExecutor {

  type QueryExecutorThreadManager = ThreadManager
  case class QueryExecutionRequest(queryId: QueryId, query: String, service: TrinoService)
}
