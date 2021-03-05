package wvlet.querybase.server.backend.query

import wvlet.airframe.codec.JDBCCodec
import wvlet.airframe.control.Control
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryId
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient
import wvlet.querybase.server.backend.ThreadManager
import wvlet.querybase.server.backend.query.QueryExecutor.{QueryExecutionRequest, QueryExecutorThreadManager}
import wvlet.querybase.server.backend.query.trino.TrinoJDBCRunner

import java.sql.SQLException
import java.time.Instant

class QueryExecutor(
    threadManager: QueryExecutorThreadManager,
    coordinatorClient: CoordinatorClient,
    trinoJDBCRunner: TrinoJDBCRunner
) extends LogSupport {

  def executeQuery(request: QueryExecutionRequest): Unit = {
    threadManager.submit(execute(request))
  }

  private def execute(request: QueryExecutionRequest): Unit = {
    info(s"Starting query: ${request.queryId}")

    coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
      queryId = request.queryId,
      status = QueryStatus.RUNNING,
      completedAt = None
    )

    trinoJDBCRunner.withConnection(request.service) { conn =>
      Control.withResource(conn.createStatement()) { stmt =>
        try {
          val rs   = stmt.executeQuery(request.query)
          val json = JDBCCodec(rs).toJsonSeq.iterator.toIndexedSeq.mkString("\n")
          info(json)
          coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
            queryId = request.queryId,
            status = QueryStatus.FINISHED,
            completedAt = Some(Instant.now())
          )
        } catch {
          case e: SQLException =>
            warn(s"${e.getMessage}")
            coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
              queryId = request.queryId,
              status = QueryStatus.FAILED,
              completedAt = Some(Instant.now())
            )
        }
      }
    }
  }
}

object QueryExecutor {

  type QueryExecutorThreadManager = ThreadManager
  case class QueryExecutionRequest(queryId: QueryId, query: String, service: TrinoService)
}
