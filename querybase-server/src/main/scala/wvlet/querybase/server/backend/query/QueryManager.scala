package wvlet.querybase.server.backend.query

import io.grpc.StatusRuntimeException
import wvlet.airframe.codec.MessageCodec
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{NewQueryRequest, NodeId, QueryId, QueryInfo}
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService
import wvlet.querybase.api.backend.v1.query.QueryStatus
import wvlet.querybase.server.backend.api.{ServiceCatalog, ServiceDef}
import wvlet.querybase.server.backend.query.QueryManager.QueryManagerThreadManager
import wvlet.querybase.server.backend.{NodeManager, RPCClientProvider}

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentHashMap, Executors}
import scala.jdk.CollectionConverters._

/**
  */
class QueryManager(
    catalog: ServiceCatalog,
    nodeManager: NodeManager,
    threadManager: QueryManagerThreadManager,
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
    val qi: QueryInfo = findService(serviceName) match {
      case Some(svc) =>
        QueryInfo(
          queryId = queryId,
          serviceName = svc.name,
          serviceType = svc.serviceType,
          queryStatus = QueryStatus.QUEUED,
          query = request.query
        )
      case None =>
        QueryInfo(
          queryId = queryId,
          serviceName = request.serviceName,
          serviceType = "N/A",
          queryStatus = QueryStatus.FAILED,
          query = request.query
        )
    }
    startNewQuery(qi)
    // TODO Process query
    qi
  }

  def listQueries: Seq[QueryInfo] = {
    queryList.values.toSeq
  }

  private val continueQueryProcessing = new AtomicBoolean(true)

  private def findService(name: String): Option[ServiceDef] = {
    catalog.services.find(_.name == name)
  }

  private def updateQuery(qi: QueryInfo): Unit = {
    queryList.put(qi.queryId, qi)
  }

  private def startNewQuery(qi: QueryInfo): Unit = {
    updateQuery(qi)

    if (!qi.queryStatus.isFinished) {
      val service = findService(qi.serviceName) match {
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
                  workerApi.v1.WorkerApi.runTrinoQuery(qi.queryId, service = trinoService, query = qi.query)
                info(executionInfo)
                queryAssignment.put(qi.queryId, RemoteQuery(executionInfo.nodeId))
                updateQuery(qi.withQueryStatus(QueryStatus.RUNNING))
              } catch {
                case e: StatusRuntimeException =>
                  warn(e.getTrailers)
                  warn(e.getStatus.getCode)
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

  type QueryManagerThreadManager = ScheduledThreadManager

  class ScheduledThreadManager extends AutoCloseable {
    private val executorService = Executors.newCachedThreadPool()
    def submit[U](body: => U): Unit = {
      executorService.submit(new Runnable {
        override def run(): Unit = body
      })
    }
    override def close(): Unit = executorService.shutdownNow()
  }

}

case class RemoteQuery(nodeId: NodeId)
