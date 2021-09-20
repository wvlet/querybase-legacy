package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime
import wvlet.querybase.api.backend.v1.query.QueryStatus

import java.time.Instant

/** */
@RPC
trait CoordinatorApi {
  import CoordinatorApi._

  def listNodes: Seq[NodeInfo]
  def register(node: Node): RegisterResponse

  def newQuery(queryRequest: NewQueryRequest): NewQueryResponse
  def getQueryInfo(queryId: String): Option[QueryInfo]
  def listQueries: Seq[QueryInfo]

  def updateQueryStatus(
      queryId: String,
      status: QueryStatus,
      error: Option[QueryError] = None,
      completedAt: Option[Instant] = None
  ): Int
}

object CoordinatorApi {
  type QueryId = String
  type NodeId  = String

  case class Node(name: NodeId, address: String, isCoordinator: Boolean, startedAt: Instant)
  case class NodeInfo(node: Node, lastHeartbeatAt: Instant) {
    def isCoordinator: Boolean = node.isCoordinator
  }
  case class RegisterResponse()

  case class NewQueryRequest(query: String, serviceName: String, schema: Option[String], limit: Option[Int])
  case class NewQueryResponse(queryId: QueryId)

  case class QueryInfo(
      queryId: QueryId,
      serviceName: String,
      serviceType: String,
      schema: String,
      queryStatus: QueryStatus,
      query: String,
      createdAt: Instant = Instant.now(),
      completedAt: Option[Instant] = None,
      error: Option[QueryError] = None,
      result: Option[QueryResult] = None,
      limit: Option[Int] = None
  ) {
    def isFinished: Boolean = queryStatus.isFinished
    def elapsed: ElapsedTime = {
      completedAt match {
        case Some(completed) =>
          ElapsedTime.succinctMillis(completed.toEpochMilli - createdAt.toEpochMilli)
        case _ =>
          ElapsedTime.succinctMillis(System.currentTimeMillis() - createdAt.toEpochMilli)
      }
    }

    def withSchema(newSchema: String): QueryInfo                    = this.copy(schema = newSchema)
    def withError(err: Option[QueryError]): QueryInfo               = this.copy(error = err)
    def withQueryStatus(newQueryStatus: QueryStatus): QueryInfo     = this.copy(queryStatus = newQueryStatus)
    def withCompletedAt(newCompletedAt: Option[Instant]): QueryInfo = this.copy(completedAt = newCompletedAt)
    def withQueryResult(newQueryResult: QueryResult): QueryInfo     = this.copy(result = Some(newQueryResult))
  }

  case class QueryResult(
      schema: Seq[Column],
      rows: Seq[Seq[Any]] = Seq.empty
  )
  case class Column(name: String, typeName: String)

  case class QueryError(
      errorCode: Int,
      errorCodeName: String,
      errorMessage: String
  )
}

object RequestStatus {
  val Ok     = 0
  val Failed = -1
}
