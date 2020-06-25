package wvlet.querybase.api.v1.query

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import wvlet.airframe.http.RPC
import wvlet.airframe.json.Json
import wvlet.airframe.metrics.{DataSize, ElapsedTime}

/**
  */
@RPC
trait QueryLogApi {
  import QueryLogApi._
  def add(request: AddQueryLogRequest): AddQueryLogResponse
}

object QueryLogApi {

  case class QueryEngine(name: String, version: String)
  case class QueryUser(groupId: String, userId: String)
  case class QueryError(errorCode: String, errorMessage: String)

  /**
    * Mandatory data for indexing queries
    */
  case class QueryIndex(
      queryEngine: QueryEngine,
      queryUser: QueryUser,
      // service cluster name
      cluster: String,
      // engine-specific query id
      queryId: String
  )

  case class QueryLog(
      queryIndex: QueryIndex,
      // Target database (or schema) of the query
      database: String,
      // Raw SQL query text
      query: String,
      status: QueryStatus,
      error: Option[QueryError] = None,
      createdAt: Instant,
      startedAt: Instant,
      endedAt: Instant,
      queryParams: Option[Json] = None
  ) {
    def wallTime: ElapsedTime   = ElapsedTime.succinctMillis(endedAt.toEpochMilli - createdAt.toEpochMilli)
    def queuedTime: ElapsedTime = ElapsedTime.succinctMillis(startedAt.toEpochMilli - createdAt.toEpochMilli)
  }

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

  case class AddQueryLogRequest(logs: Seq[QueryLog], uuid: UUID = UUID.randomUUID())
  case class AddQueryLogResponse(uuid: UUID)

}
