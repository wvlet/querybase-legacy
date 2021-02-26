package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC

/**
  */
@RPC
trait CoordinatorApi {
  import CoordinatorApi._

  def listNodes: Seq[Node]
  def register(node: Node): RegisterResponse
}

object CoordinatorApi {

  case class Node(name: String, address: String, isCoordinator: Boolean)

  case class RegisterResponse()

}
