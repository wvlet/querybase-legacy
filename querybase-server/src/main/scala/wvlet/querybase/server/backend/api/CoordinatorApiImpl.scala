package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi
import wvlet.querybase.server.backend.NodeManager
import wvlet.querybase.server.backend.query.QueryManager

/**
  */
class CoordinatorApiImpl(nodeManager: NodeManager, queryManager: QueryManager) extends CoordinatorApi with LogSupport {
  import CoordinatorApi._

  override def listNodes: Seq[NodeInfo] = {
    nodeManager.listNodes
  }

  override def register(node: Node): RegisterResponse = {
    nodeManager.heartBeat(node)
    RegisterResponse()
  }

  override def newQuery(queryRequest: NewQueryRequest): NewQueryResponse = {
    val qi = queryManager.newQuery(queryRequest)
    NewQueryResponse(qi.queryId.toString)
  }

  override def listQueries: Seq[QueryInfo] = {
    queryManager.listQueries
  }
}
