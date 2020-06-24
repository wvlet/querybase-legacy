package wvlet.querybase.api.v1.query

import java.time.Instant
import java.util.UUID

import wvlet.airframe.http.RPC
import wvlet.airframe.json.Json
import wvlet.airframe.metrics.{DataSize, ElapsedTime}

/**
  */
@RPC
trait QueryLogApi {
  import QueryLogApi._

  def list: Seq[QueryLog]
  def add(request: AddQueryLogRequest): AddQueryLogResponse
}

object QueryLogApi {

  case class QueryLog(engine: String,
                      groupId: Option[String],
                      userId: Option[String],
                      queryId: String,
                      query: String,
                      database: String,
                      status: QueryStatus,
                      errorCode: Option[String],
                      createdAt: Instant,
                      startedAt: Instant,
                      endedAt: Instant,
                      queryParams: Option[Json]) {
    def wallTime: ElapsedTime   = ElapsedTime.succinctMillis(endedAt.toEpochMilli - createdAt.toEpochMilli)
    def queuedTime: ElapsedTime = ElapsedTime.succinctMillis(startedAt.toEpochMilli - createdAt.toEpochMilli)
  }

  case class PrestoQueryStat(
      queryId: String,
      wallTimeMillis: Double,
      splitWallTimeMillis: Double,
      cpuTimeMillis: Double,
      peakMemory: DataSize,
      cumulativeMemoryGBSec: Double,
      processedRows: Long,
      processedBytes: DataSize,
      outputRows: Long,
      outputBytes: DataSize
  )

  case class TableScanLog(
      groupId: String,
      userId: String,
      queryId: String,
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
