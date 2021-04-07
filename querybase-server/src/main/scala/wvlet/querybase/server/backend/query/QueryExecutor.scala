package wvlet.querybase.server.backend.query

import io.trino.spi.StandardErrorCode
import org.xerial.snappy.SnappyOutputStream
import wvlet.airframe.codec.JDBCCodec
import wvlet.airframe.control.Control
import wvlet.airframe.msgpack.spi.MessagePack
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{QueryError, QueryId}
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient
import wvlet.querybase.server.backend.ThreadManager
import wvlet.querybase.server.backend.query.QueryExecutor.{QueryExecutionRequest, QueryExecutorThreadManager}
import wvlet.querybase.server.backend.query.trino.TrinoJDBCRunner

import java.io.{File, FileOutputStream}
import java.sql.SQLException
import java.time.Instant

case class QueryExecutorConfig(
    queryResultStorePath: File = new File(".querybase/results")
)

class QueryExecutor(
    queryResultStore: QueryResultStore,
    threadManager: QueryExecutorThreadManager,
    coordinatorClient: CoordinatorClient,
    trinoJDBCRunner: TrinoJDBCRunner
) extends LogSupport {

  def executeQuery(request: QueryExecutionRequest): Unit = {
    threadManager.submit(execute(request))
  }

  private def execute(request: QueryExecutionRequest): Unit = {
    info(s"Starting query: ${request.queryId}")

    // TODO Make this an asynchronous call to avoid the latency before query processing
    coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
      queryId = request.queryId,
      status = QueryStatus.RUNNING,
      error = None,
      completedAt = None
    )

    // Prepare query result store
    val queryResultFile = queryResultStore.createNewResultFile(request.queryId)

    Control.withResource(MessagePack.newPacker(new SnappyOutputStream(new FileOutputStream(queryResultFile)))) {
      packer =>
        trinoJDBCRunner.withConnection(request.service) { conn =>
          Control.withResource(conn.createStatement()) { stmt =>
            try {
              val rs          = stmt.executeQuery(request.query)
              val md          = rs.getMetaData
              val columnCount = rs.getMetaData.getColumnCount

              info(s"Writing the query result to ${queryResultFile}")
              // Output schema
              packer.packArrayHeader(columnCount)
              (1 to columnCount).map { i =>
                packer.packArrayHeader(2)
                packer.packString(md.getColumnName(i))
                packer.packString(md.getColumnTypeName(i))
              }

              val rsCodec  = JDBCCodec(rs)
              var rowCount = 0
              val maxRows  = request.executionType.resultRowsLimit.getOrElse(Int.MaxValue)
              while (rs.next() && rowCount < maxRows) {
                rsCodec.packRowAsArray(packer)
                rowCount += 1
              }

              // Send a query cancel request if it still has more rows
              if (rs.next()) {
                warn(f"Read ${rowCount}%,d rows limit for preview. Cancelling the query: ${request.queryId}")
                stmt.cancel()
              }
              rs.close()
              coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
                queryId = request.queryId,
                status = QueryStatus.FINISHED,
                error = None,
                completedAt = Some(Instant.now())
              )
            } catch {
              case e: SQLException =>
                warn(s"${e.getMessage}")
                val err = QueryError(
                  errorCode = e.getErrorCode,
                  // TODO Resolve error code name. Currently, trino-jdbc provides no mapping to StandardErrorCode
                  errorCodeName = "N/A",
                  errorMessage = e.getMessage
                )
                coordinatorClient.v1.CoordinatorApi.updateQueryStatus(
                  queryId = request.queryId,
                  status = QueryStatus.FAILED,
                  error = Some(err),
                  completedAt = Some(Instant.now())
                )
            }
          }
        }
    }
  }
}

object QueryExecutor {

  type QueryExecutorThreadManager = ThreadManager
  case class QueryExecutionRequest(
      queryId: QueryId,
      query: String,
      service: TrinoService,
      executionType: ExecutionType = PREVIEW(limit = 1000)
  )

  sealed trait ExecutionType {
    def resultRowsLimit: Option[Int]
  }
  case class PREVIEW(limit: Int = 1000) extends ExecutionType {
    override def resultRowsLimit: Option[Int] = Some(limit)
  }
  case object FULL extends ExecutionType {
    override def resultRowsLimit: Option[Int] = None
  }

}
