package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime
import wvlet.querybase.api.backend.v1.query.QueryStatus

import java.time.Instant

/**
  */
@RPC
trait CoordinatorApi {
  import CoordinatorApi._

  def listNodes: Seq[NodeInfo]
  def register(node: Node): RegisterResponse

  def newQuery(queryRequest: NewQueryRequest): NewQueryResponse
  def listQueries: Seq[QueryInfo]
}

object CoordinatorApi {

  case class Node(name: String, address: String, isCoordinator: Boolean, startedAt: Instant)
  case class NodeInfo(node: Node, lastHeartbeatAt: Instant)
  case class RegisterResponse()

  case class NewQueryRequest(query: String, serviceName: String)
  case class NewQueryResponse(queryId: String)

  case class QueryInfo(
      queryId: String,
      serviceName: String,
      serviceType: String,
      queryStatus: QueryStatus,
      query: String,
      createdAt: Instant = Instant.now(),
      completedAt: Option[Instant] = None
  ) {
    def elapsed: ElapsedTime = {
      completedAt match {
        case Some(completed) =>
          ElapsedTime.succinctMillis(completed.toEpochMilli - createdAt.toEpochMilli)
        case _ =>
          ElapsedTime.succinctMillis(System.currentTimeMillis() - createdAt.toEpochMilli)
      }
    }

    def withQueryStatus(newQueryStatus: QueryStatus): QueryInfo = this.copy(queryStatus = newQueryStatus)
    def withCompletedAt(completedAt: Instant): QueryInfo        = this.copy(completedAt = Some(completedAt))
  }
}
