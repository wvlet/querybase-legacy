package wvlet.querybase.api.v1.query

import java.time.Instant
import java.util.UUID

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.{DataSize, ElapsedTime}

/**
  * API for managing query logs
  */
@RPC
trait QueryLogApi {
  import QueryLogApi._
  def add(request: AddQueryLogRequest): AddQueryLogResponse
}

object QueryLogApi {

  case class QueryUser(
      // user group id, such as organization id, account id, etc.
      groupId: String,
      userId: String
  )
  case class QueryEngine(name: String, version: String)
  case class QueryError(errorCode: String, errorMessage: String)

  /**
    * Mandatory data for indexing queries
    */
  case class QueryIndex(
      // engine-specific query id
      queryId: String,
      queryUser: QueryUser,
      queryEngine: QueryEngine,
      // service cluster name
      cluster: String,
      // Extra tags for categorizing query execution (e.g., benchmark_id, simulation_id, etc.)
      executionParams: Map[String, Any] = Map.empty
  )

  case class Query(
      // Context database name
      database: String,
      // Raw SQL text
      queryText: String,
      // Additional query parameters (e.g., job_id, resource_group_name, workflow_id, session_id, session parameters, etc.)
      queryParams: Map[String, Any] = Map.empty
  )

  /**
    * Engine-agonistic query log format
    */
  case class QueryLog(
      queryIndex: QueryIndex,
      // Target database (or schema) of the query
      database: String,
      query: Query,
      status: QueryStatus,
      error: Option[QueryError] = None,
      createdAt: Instant,
      startedAt: Instant,
      endedAt: Instant
  ) {
    def wallTime: ElapsedTime   = ElapsedTime.succinctMillis(endedAt.toEpochMilli - createdAt.toEpochMilli)
    def queuedTime: ElapsedTime = ElapsedTime.succinctMillis(startedAt.toEpochMilli - createdAt.toEpochMilli)
  }

  /**
    * Engine-agonistic table scan log format
    */
  case class TableScanLog(
      queryIndex: QueryIndex,
      database: String,
      table: String,
      columns: Seq[String],
      predicates: Option[String],
      timeRangeStart: Option[Instant],
      timeRangeEnd: Option[Instant],
      inputRows: Long,
      outputRows: Long,
      inputBytes: Long,
      outputBytes: Long
  ) {
    def rowSkipRatio: Double  = 1.0 - (outputRows.toDouble / inputRows)
    def byteSkipRatio: Double = 1.0 - (outputBytes.toDouble / inputBytes)
    def timeWindowSize: Option[ElapsedTime] = {
      (timeRangeStart, timeRangeEnd) match {
        case (Some(s), Some(e)) =>
          Some(ElapsedTime.succinctMillis(e.toEpochMilli - s.toEpochMilli))
        case _ =>
          None
      }
    }
  }

  /**
    * Presto-specific query stats
    */
  case class PrestoQueryStats(
      queryIndex: QueryIndex,
      wallTimeMillis: Double,
      splitWallTimeMillis: Double,
      splitBlockedTimeMillis: Double,
      cpuTimeMillis: Double,
      peakMemory: DataSize,
      cumulativeMemoryGBSec: Double,
      processedRows: Long,
      processedBytes: DataSize,
      outputRows: Long,
      outputBytes: DataSize
  ) {
    def wallTime = ElapsedTime.succinctMillis(wallTimeMillis.toLong)
  }

  case class AddQueryLogRequest(logs: Seq[QueryLog], uuid: UUID = UUID.randomUUID())
  case class AddQueryLogResponse(uuid: UUID)

}
