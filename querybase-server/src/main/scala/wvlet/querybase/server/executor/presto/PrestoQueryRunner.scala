package wvlet.querybase.server.executor.presto

import java.net.URI
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import java.util.{Locale, Optional}

import io.prestosql.client.{ClientSelectedRole, ClientSession, StatementClient, StatementClientFactory}
import okhttp3.OkHttpClient
import wvlet.airframe.surface.secret
import wvlet.airframe._
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.codec.PrimitiveCodec.ValueCodec
import wvlet.airframe.msgpack.spi.{MsgPack, Value}
import wvlet.log.LogSupport

import scala.jdk.CollectionConverters._

case class PrestoQueryRequest(
    coordinatorAddress: String,
    user: String,
    @secret password: Option[String] = None,
    sql: String,
    catalog: String,
    schema: String = "information_schema"
) {
  def withUser(newUser: String): PrestoQueryRequest = {
    this.copy(user = newUser)
  }

  def withPassword(newPassword: String): PrestoQueryRequest = {
    this.copy(password = Some(newPassword))
  }

  def toClientSession: ClientSession = {
    new ClientSession(
      // server
      new URI(coordinatorAddress),
      user,
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
      io.airlift.units.Duration.valueOf("2m")
    )
  }
}

object PrestoQueryRunner {

  def design: Design =
    OkHttpClientService.design
      .bind[PrestoQueryRunner].toSingleton

}

/**
  */
class PrestoQueryRunner(okHttpClient: OkHttpClient) {
  def startQuery(r: PrestoQueryRequest): PrestoQueryContext = {
    new PrestoQueryContext(StatementClientFactory.newStatementClient(okHttpClient, r.toClientSession, r.sql))
  }
}

class PrestoQueryContext(private val statementClient: StatementClient) extends AutoCloseable with LogSupport {

  private val rowCodec = MessageCodec.of[Seq[Any]]

  def run: Unit = {

    def readRows: Seq[MsgPack] = {
      val msgpackRowSeq = Option(statementClient.currentData().getData)
        .map { data =>
          val status = statementClient.currentStatusInfo()
          info(status.getStats)
          val msgpackRows = data.asScala.map { row =>
            val rowSeq = row.asScala.toSeq
            rowCodec.toMsgPack(rowSeq)
          }
          msgpackRows.toSeq
        }.getOrElse(Seq.empty)
      msgpackRowSeq
    }

//    if(statementClient.isRunning || (statementClient.isFinished && statementClient.finalStatusInfo().getError == null)) {
//      val status = if(statementClient.isRunning) statementClient.currentStatusInfo() else statementClient.finalStatusInfo()
//      info(status.getStats)
//    }
//

    var readSchema = false

    while (statementClient.isRunning) {
      val status = statementClient.currentStatusInfo()
      info(status.getStats)

      if (!readSchema) {
        Option(status.getColumns).foreach { columns =>
          val schema = status.getColumns.asScala.toSeq.map(x => s"${x.getName}:${x.getType}").mkString(", ")
          info(schema)
          readSchema = true
        }
      }

      val msgpack = readRows
      val values = msgpack
        .map { row =>
          ValueCodec.fromMsgPack(row)
        }
      info(values.mkString("\n"))
      statementClient.advance()
    }

    val lastStatus = statementClient.finalStatusInfo()
    info(lastStatus.getStats)
  }

  override def close(): Unit = {
    statementClient.close()
  }
}

object OkHttpClientService extends LogSupport {
  def design: Design =
    Design.newDesign
      .bind[OkHttpClient].toInstance {
        info(s"Creating a new OkHttp client")
        val builder = new OkHttpClient.Builder
        builder
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.MINUTES)
          .writeTimeout(1, TimeUnit.MINUTES)
        builder.build
      }
      .onShutdown { okHttpClient =>
        info(s"Closing OkHttp client")
        okHttpClient.dispatcher().executorService().shutdown()
        okHttpClient.connectionPool().evictAll()
      }
}
