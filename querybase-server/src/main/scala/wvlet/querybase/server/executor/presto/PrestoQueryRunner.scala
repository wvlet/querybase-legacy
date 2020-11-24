package wvlet.querybase.server.executor.presto

import java.net.URI
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import java.util.{Locale, Optional}

import io.prestosql.client.{ClientSelectedRole, ClientSession, StatementClient, StatementClientFactory}
import okhttp3.OkHttpClient
import wvlet.airframe.surface.secret
import wvlet.airframe._
import wvlet.airframe.control.Control

import scala.jdk.CollectionConverters._

case class PrestoQueryRequest(
    coordinatorAddress: String,
    user: String,
    @secret password: Option[String] = None,
    sql: String,
    catalog: String,
    schema: String
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

/**
  *
  */
class PrestoQueryRunner(okHttpClient: OkHttpClient) {
  def startQuery(r: PrestoQueryRequest): PrestoQueryContext = {
    new PrestoQueryContext(StatementClientFactory.newStatementClient(okHttpClient, r.toClientSession, r.sql))
  }
}

class PrestoQueryContext(statementClient: StatementClient) extends AutoCloseable {

  override def close(): Unit = {
    statementClient.close()
  }
}

object OkHttpClientService {
  def design: Design =
    Design.newDesign
      .bind[OkHttpClient].toInstance {
        val builder = new OkHttpClient.Builder
        builder
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.MINUTES)
          .writeTimeout(1, TimeUnit.MINUTES)
        builder.build
      }
      .onShutdown { okHttpClient =>
        okHttpClient.dispatcher().executorService().shutdown()
        okHttpClient.connectionPool().evictAll()
      }
}
