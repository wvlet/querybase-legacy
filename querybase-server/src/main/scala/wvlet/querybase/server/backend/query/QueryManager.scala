package wvlet.querybase.server.backend.query

import io.grpc.StatusRuntimeException
import wvlet.airframe.codec.MessageCodec
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, NodeId, QueryId, QueryInfo}
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.api.{ServiceCatalog, ServiceDef}
import wvlet.querybase.server.backend.query.QueryManager.QueryManagerThreadManager
import wvlet.querybase.server.backend.{NodeManager, RPCClientProvider, ThreadManager}

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.jdk.CollectionConverters._

/** */
class QueryManager(
    catalog: ServiceCatalog,
    nodeManager: NodeManager,
    threadManager: QueryManagerThreadManager,
    queryLogger: QueryLogger,
    rpcClientProvider: RPCClientProvider
) extends LogSupport
    with AutoCloseable {

  private val queryList        = new ConcurrentHashMap[QueryId, QueryInfo]().asScala
  private val queryAssignment  = new ConcurrentHashMap[QueryId, RemoteQuery]()
  private val queryIdGenerator = new QueryIdGenerator()

  threadManager.submit(processQueries)

  def newQuery(request: NewQueryRequest): QueryInfo = {
    val queryId: QueryId = queryIdGenerator.newQueryId

    // Find the target service from the catalog
    val serviceName = request.serviceName
    val schema      = request.schema.getOrElse("information_schema")
    val qi: QueryInfo = findService(serviceName) match {
      case Some(svc) =>
        QueryInfo(
          queryId = queryId,
          serviceName = svc.name,
          serviceType = svc.serviceType,
          schema = schema,
          queryStatus = QueryStatus.QUEUED,
          query = request.query
        )
      case None =>
        QueryInfo(
          queryId = queryId,
          serviceName = request.serviceName,
          serviceType = "N/A",
          schema = schema,
          queryStatus = QueryStatus.FAILED,
          query = request.query
        )
    }
    queryLogger.startLog(qi)
    startNewQuery(qi)
    // TODO Process query
    qi
  }

  def getQueryInfo(queryId: String): Option[QueryInfo] = {
    queryList.get(queryId)
  }

  def listQueries: Seq[QueryInfo] = {
    queryList.values.toSeq
  }

  private val continueQueryProcessing = new AtomicBoolean(true)

  private def findService(name: String): Option[ServiceDef] = {
    catalog.services.find(_.name == name)
  }

  private[backend] def update(queryId: QueryId)(updater: QueryInfo => QueryInfo): Option[QueryInfo] = {
    queryList.get(queryId).map { qi =>
      val updated = updater(qi)
      queryList.put(queryId, updated)
      if (updated.isFinished) {
        queryLogger.completionLog(updated)
      }
      updated
    }
  }

  private[backend] def updateQuery(qi: QueryInfo): Unit = {
    queryList.put(qi.queryId, qi)
  }

  private def startNewQuery(qi: QueryInfo): Unit = {
    updateQuery(qi)

    if (!qi.queryStatus.isFinished) {
      findService(qi.serviceName) match {
        case None =>
          updateQuery(qi.withQueryStatus(QueryStatus.FAILED))
        case Some(svc) =>
          val workerNodes = nodeManager.listNodes.filter(!_.isCoordinator)
          // TODO Pick a non-busy worker node
          val w = workerNodes.head

          qi.serviceType match {
            case "trino" =>
              val trinoService = MessageCodec.of[TrinoService].fromJson(svc.properties)
              val workerApi    = rpcClientProvider.getSyncClientFor(w.node.address)
              try {
                val executionInfo =
                  workerApi.v1.WorkerApi.runTrinoQuery(
                    qi.queryId,
                    service = trinoService,
                    query = qi.query,
                    schema = qi.schema
                  )
                queryAssignment.put(qi.queryId, RemoteQuery(executionInfo.nodeId))
              } catch {
                case e: StatusRuntimeException =>
                  warn(e.getMessage)
                  updateQuery(qi.withQueryStatus(QueryStatus.FAILED))
              }
            case other =>
              warn(s"Not supported service type: ${other}")
          }
      }
    }
  }

  private def processQueries: Unit = {
    while (continueQueryProcessing.get()) {
      val snapshot      = queryList.values.toIndexedSeq
      val activeQueries = snapshot.filter(!_.queryStatus.isFinished)
      //info(s"Active queries: ${activeQueries.size}")
      Thread.sleep(3000)
    }
  }

  override def close(): Unit = {
    info(s"Closing QueryManager")
    continueQueryProcessing.set(false)
  }
}

object QueryManager {

  type QueryManagerThreadManager = ThreadManager

}

case class RemoteQuery(nodeId: NodeId)
