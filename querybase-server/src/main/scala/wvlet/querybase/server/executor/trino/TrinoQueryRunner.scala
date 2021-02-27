package wvlet.querybase.server.executor.trino

import java.net.URI
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import java.util.{Locale, Optional}
import io.trino.client.{ClientSelectedRole, ClientSession, QueryData, StatementClient, StatementClientFactory}
import okhttp3.OkHttpClient
import wvlet.airframe.surface.secret
import wvlet.airframe._
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.codec.PrimitiveCodec.ValueCodec
import wvlet.airframe.msgpack.spi.{MsgPack, Value}
import wvlet.log.LogSupport

import scala.jdk.CollectionConverters._

case class TrinoQueryRequest(
    coordinatorAddress: String,
    user: String,
    @secret password: Option[String] = None,
    sql: String,
    catalog: String,
    schema: String = "information_schema"
) {
  def withUser(newUser: String): TrinoQueryRequest = {
    this.copy(user = newUser)
  }

  def withPassword(newPassword: String): TrinoQueryRequest = {
    this.copy(password = Some(newPassword))
  }

  def toClientSession: ClientSession = {
    new ClientSession(
      // server
      new URI(coordinatorAddress),
      "",
      Optional.of(user),
      "querybase",
      // traceToken
      Optional.empty(),
      // clientTags
      Set.empty[String].asJava,
      null,
      catalog,
      schema,
      null,
      ZoneOffset.UTC,
      Locale.ENGLISH,
      // resource estimates
      Map.empty[String, String].asJava,
      // properties
      Map.empty[String, String].asJava,
      // preparedStatements
      Map.empty[String, String].asJava,
      // roles
      Map.empty[String, ClientSelectedRole].asJava,
      // extra credentials
      Map.empty[String, String].asJava,
      // transaction id
      null,
      // client request timeout
      io.airlift.units.Duration.valueOf("2m"),
      false
    )
  }
}

object TrinoQueryRunner {

  def design: Design =
    OkHttpClientService.design
      .bind[TrinoQueryRunner].toSingleton

}

/**
  */
class TrinoQueryRunner(okHttpClient: OkHttpClient) {
  def startQuery(r: TrinoQueryRequest): TrinoQueryContext = {
    new TrinoQueryContext(StatementClientFactory.newStatementClient(okHttpClient, r.toClientSession, r.sql))
  }
}

class TrinoQueryContext(private val statementClient: StatementClient) extends AutoCloseable with LogSupport {

  private val rowCodec = MessageCodec.of[Seq[Any]]

  def run: Unit = {

    def readRows(data: java.lang.Iterable[java.util.List[AnyRef]]): Seq[MsgPack] = {
      val row = data.asScala
      val msgpackRows = data.asScala.map { row =>
        val rowSeq = row.asScala.toSeq
        rowCodec.toMsgPack(rowSeq)
      }
      msgpackRows.toSeq
    }

//    if(statementClient.isRunning || (statementClient.isFinished && statementClient.finalStatusInfo().getError == null)) {
//      val status = if(statementClient.isRunning) statementClient.currentStatusInfo() else statementClient.finalStatusInfo()
//      info(status.getStats)
//    }
//

//    while (statementClient.isRunning && (statementClient.currentData().getData() == null)) {
//      statementClient.advance()
//    }

    var readSchema = false
    while (statementClient.isRunning) {
      val status = statementClient.currentStatusInfo()
      if (!readSchema) {
        Option(status.getColumns).foreach { columns =>
          val schema = status.getColumns.asScala.toSeq.map(x => s"${x.getName}:${x.getType}").mkString(", ")
          info(schema)
          readSchema = true
        }
      }

      val data = statementClient.currentData().getData
      if (data != null) {
        val rows = readRows(data)
        rows.map { row =>
          val v = ValueCodec.fromMsgPack(row)
          info(v)
        }
      }
      statementClient.advance()
    }
    val lastStatus = statementClient.finalStatusInfo()
    debug(lastStatus.getStats)
  }

  override def close(): Unit = {
    statementClient.close()
  }
}
