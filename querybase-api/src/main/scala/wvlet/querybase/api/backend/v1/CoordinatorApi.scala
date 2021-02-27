package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC

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

  case class Node(name: String, address: String, isCoordinator: Boolean)
  case class NodeInfo(node: Node, lastHeartBeat: Long)
  case class RegisterResponse()

  case class NewQueryRequest(query: String)
  case class NewQueryResponse(queryId: String)

  case class QueryInfo(queryId: String, query: String)
}
