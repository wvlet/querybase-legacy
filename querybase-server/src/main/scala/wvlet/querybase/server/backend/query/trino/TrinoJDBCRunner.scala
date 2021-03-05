package wvlet.querybase.server.backend.query.trino

import io.trino.jdbc.{TrinoConnection, TrinoDriver}
import wvlet.airframe.control.Control
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.WorkerApi.TrinoService

import java.sql.DriverManager
import java.util.Properties

class TrinoJDBCRunner(driver: TrinoJDBCDriver) {

  def withConnection[U](service: TrinoService)(body: TrinoConnection => U): U = {
    Control.withResource(driver.newConnection(service.address, service.connector, service.schema, service.user)) {
      conn =>
        body(conn)
    }
  }
}

class TrinoJDBCDriver extends AutoCloseable with LogSupport {

  private val driver: TrinoDriver = {
    info("Initializing TrinoDriver")
    Class.forName("io.trino.jdbc.TrinoDriver")
    // Need to cast to TrinoDriver to set session properties
    DriverManager.getDriver(s"jdbc:trino://").asInstanceOf[TrinoDriver]
  }

  def newConnection(hostname: String, catalog: String, schema: String, user: String): TrinoConnection = {
    val p = new Properties()
    p.setProperty("user", user)
    val connectAddress = s"jdbc:trino://${hostname}/${catalog}/${schema}"
    info(s"${connectAddress}")
    val conn = driver.connect(connectAddress, p).asInstanceOf[TrinoConnection]
    conn
  }

  override def close(): Unit = {
    info(s"Closing TrinoJDBCDriver")
    driver.close()
  }
}
