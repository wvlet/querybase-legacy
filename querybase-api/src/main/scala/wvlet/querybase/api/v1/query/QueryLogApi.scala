package wvlet.querybase.api.v1.query

import java.time.Instant
import java.util.UUID

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.{DataSize, ElapsedTime}
import wvlet.querybase.api.v1.query.QueryLogApi.presto.{PrestoQueryStageStats, PrestoQueryStats}

/**
  * API for collecting query logs
  */
@RPC
trait QueryLogApi {
  import QueryLogApi._
  def addQueryLog(request: AddQueryLogRequest): AddQueryLogResponse
  def addPrestoQueryStats(request: AddPrestoQueryStatsRequest): AddPrestoQueryStatsResponse
  def addPrestoQueryStageStats(request: AddPrestoQueryStageStatsRequest): AddPrestoQueryStageStatsResponse
}

object QueryLogApi {

  case class QueryUser(
      // user group id, such as organization id, account id, etc.
      groupId: String,
      // system-specific user id
      userId: String
  )
  case class QueryEngine(name: String, version: String)
  case class QueryError(errorCode: String, errorMessage: String)
  case class QueryTarget(catalog: String, database: String)

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
      // Target catalog and database (or schema) of the query
      queryTarget: QueryTarget,
      // Input query info
      query: Query,
      // Generic query status
      status: QueryStatus,
      // Any error if exists
      error: Option[QueryError] = None,
      // Time when the query was created, but not yet started (i.e., queued state)
      // Note: We use Instant for time representation because ZonedDateTime is not supported in Scala.js
      createdAt: Instant,
      // Time when the query started running
      startedAt: Instant,
      // Time when the query finished
      endedAt: Instant
  ) {
    def wallTime: ElapsedTime   = ElapsedTime.succinctMillis(endedAt.toEpochMilli - createdAt.toEpochMilli)
    def queuedTime: ElapsedTime = ElapsedTime.succinctMillis(startedAt.toEpochMilli - createdAt.toEpochMilli)
  }

  /**
    * Table scan range for time-series data
    */
  case class TableScanRange(start: Option[Instant] = None, end: Option[Instant] = None) {
    (start, end) match {
      case (Some(s), Some(e)) =>
        require(s.compareTo(e) <= 0, s"Table scan range must be start <= end: ${this}")
      case _ =>
      // Ok
    }

    def timeWindowSize: Option[ElapsedTime] = {
      (start, end) match {
        case (Some(s), Some(e)) =>
          Some(ElapsedTime.succinctMillis(e.toEpochMilli - s.toEpochMilli))
        case _ =>
          None
      }
    }
  }

  /**
    * Input and output amount of data in a query stage
    */
  case class InOut(input: Long, processed: Long, output: Long) {
    def skipRatio: Double = 1.0 - (output.toDouble / input)
  }

  /**
    * Engine-agonistic table scan log format
    */
  case class TableScanLog(
      queryIndex: QueryIndex,
      // Target catalog and database (or schema) of the query
      queryTarget: QueryTarget,
      // Scanned table name
      table: String,
      // Internal table id (e.g., dataset_id)
      tableId: String,
      // The list of columns scanned (projected columns)
      columns: Seq[String],
      // Predicate for the columns (e.g., where condition or predicate used for push-down optimization)
      predicates: Option[String],
      // Table scan range for time-series data
      scanRange: TableScanRange = TableScanRange(None, None),
      // The number of partition files scanned
      numPartitions: Long,
      // The number of tasks used for scanning the table
      numTasks: Long,
      // The number of input/output rows
      inOutRows: InOut,
      // The number of input/output bytes
      inOutBytes: InOut,
      // CPU time used for scanning the table
      cpuTimeMillis: Long,
      // User clock time used for scanning the table
      userTimeMillis: Long,
      // Waiting time while scanning the table
      blockedTimeMillis: Long,
      // Extra table scan parameters
      tableScanParams: Map[String, Any] = Map.empty
  ) {
    def rowSkipRatio: Double                = inOutRows.skipRatio
    def byteSkipRatio: Double               = inOutBytes.skipRatio
    def timeWindowSize: Option[ElapsedTime] = scanRange.timeWindowSize
  }

  /**
    * Presto-specific query stats
    */
  object presto {
    case class PrestoPerfStats(
        wallTimeMillis: Long,
        analysisTimeMillis: Double,
        planningTimeMillis: Double,
        queuedTimeMillis: Long,
        scheduledTimeMillis: Long,
        cpuTimeMillis: Long,
        blockedTimeMillis: Long,
        processedRows: Long,
        processedBytes: Long
    )

    case class PrestoQueryStats(
        queryIndex: QueryIndex,
        performanceStats: PrestoPerfStats,
        peakMemory: DataSize,
        cumulativeMemoryGBSec: Double,
        numSplits: Long
    ) {
      def wallTime = ElapsedTime.succinctMillis(performanceStats.wallTimeMillis)
    }

    case class Percentiles(distribution: Seq[Double])

    case class PartitionAccessStats(
        numReadPartitions: Long,
        numReadRequestCount: Long,
        numReadRequestRetryCount: Long,
        headerReadTimeMillis: Long,
        columnBlockReadTimeMillis: Long,
        numWritePartitions: Long,
    )

    case class PrestoQueryStageStats(
        queryIndex: QueryIndex,
        stageId: String,
        numSplits: String,
        performanceStats: PrestoPerfStats,
        processedRowsPercentiles: Percentiles,
        processedBytesPercentiles: Percentiles,
        rowsPerSecPercentiles: Percentiles,
        bytesPerSecPercentiles: Percentiles,
        peakMemory: DataSize,
        partitionAccessStats: Option[PartitionAccessStats] = None
    )
  }

  case class AddQueryLogRequest(logs: Seq[QueryLog], uuid: UUID = UUID.randomUUID())
  case class AddQueryLogResponse(uuid: UUID)

  case class AddPrestoQueryStatsRequest(logs: Seq[PrestoQueryStats], uuid: UUID = UUID.randomUUID())
  case class AddPrestoQueryStatsResponse(uuid: UUID)

  case class AddPrestoQueryStageStatsRequest(logs: Seq[PrestoQueryStageStats], uuid: UUID = UUID.randomUUID())
  case class AddPrestoQueryStageStatsResponse(uuid: UUID)

}
