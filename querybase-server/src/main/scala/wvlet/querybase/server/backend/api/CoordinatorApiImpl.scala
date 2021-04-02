package wvlet.querybase.server.backend.api

import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.{CoordinatorApi, RequestStatus}
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.NodeManager
import wvlet.querybase.server.backend.query.QueryManager

import java.time.Instant

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
    NewQueryResponse(qi.queryId)
  }

  override def getQueryInfo(queryId: String): Option[QueryInfo] = {
    queryManager.getQueryInfo(queryId)
  }

  override def listQueries: Seq[QueryInfo] = {
    queryManager.listQueries
  }

  override def updateQueryStatus(queryId: String, status: QueryStatus, completedAt: Option[Instant]): Int = {
    queryManager.update(queryId) { qi =>
      qi.withQueryStatus(status).withCompletedAt(completedAt)
    }
    info(s"Update query status: ${queryId}, status: ${status}")
    //ret match {
///      case Some(qi) => RequestStatus.Ok
    //   case _        => RequestStatus.Failed
    //}
    0
  }

}
