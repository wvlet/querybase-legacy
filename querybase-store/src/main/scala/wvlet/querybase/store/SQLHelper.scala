package wvlet.querybase.store

import java.sql.Connection

import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.control.Control
import wvlet.airframe.control.Control.withResource
import wvlet.airframe.jdbc.ConnectionPool
import wvlet.airframe.surface.{Primitive, Surface}
import wvlet.log.LogSupport

/** */
object SQLHelper extends LogSupport {

  import scala.reflect.runtime.{universe => ru}

  def sqlTypeOf(tpe: Surface): String = {
    tpe match {
      case Primitive.Int     => "integer"
      case Primitive.Long    => "integer"
      case Primitive.Float   => "float"
      case Primitive.Double  => "float"
      case Primitive.Boolean => "boolean"
      case Primitive.String  => "string"
      case _                 => "string"
    }
  }

  def createTableSQLFor[A: ru.TypeTag](tableName: String, paramOption: Map[String, String] = Map.empty): String = {
    val surface = Surface.of[A]
    val params = for (p <- surface.params) yield {
      s"${p.name} ${sqlTypeOf(p.surface)} ${paramOption.getOrElse(p.name, "")}"
    }
    s"create table if not exists ${tableName} (${params.mkString(", ")})"
  }

  def quote(s: String) = s"'${s}'"

  def insertRecord[A: ru.TypeTag](tableName: String, obj: A)(implicit conn: Connection) {
    val surface = Surface.of[A]
    val colSize = surface.params.size
    val tuple   = ("?" * colSize).mkString(", ")
    withResource(conn.prepareStatement(s"insert into ${tableName} values(${tuple})")) { prep =>
      for (p <- surface.params) yield {
        val v = p.get(obj)
        prep.setObject(p.index + 1, v)
      }
      prep.execute()
    }
  }

  def deleteRecord(tableName: String, idColumn: String, id: Any)(implicit conn: Connection): Unit = {
    withResource(conn.prepareStatement(s"""delete from ${tableName} where "${idColumn}" = ?""")) { ps =>
      ps.setObject(1, id)
      ps.execute()
    }
  }

  def updateRecord[A: ru.TypeTag](tableName: String, obj: A, idColumn: String)(implicit conn: Connection): Unit = {
    val surface = Surface.of[A]
    updateColumns[A](tableName, obj, idColumn, columnMask = surface.params.map(_.name).filterNot(_ == idColumn))
  }

  def updateColumns[A: ru.TypeTag](tableName: String, obj: A, idColumn: String, columnMask: Seq[String])(implicit
      conn: Connection
  ) {
    val surface = Surface.of[A]
    val updateColumns = columnMask
      .map { column =>
        s""""${column}" = ?"""
      }.mkString(", ")

    val sql = s"""update "${tableName}" set ${updateColumns} where "${idColumn}" = ?"""
    info(sql)

    withResource(conn.prepareStatement(sql)) { prep =>
      var index = 1
      columnMask.map { column =>
        surface.params.find(_.name == column).foreach { p =>
          val v = p.get(obj)
          prep.setObject(index, v)
          index += 1
        }
      }
      surface.params.find(_.name == idColumn).foreach { p =>
        val v = p.get(obj)
        prep.setObject(index, v)
      }
      prep.execute()
    }
  }

  def readAs[A: ru.TypeTag](tableName: String, sql: String)(implicit conn: Connection): Seq[A] = {
    readStreamOf[A, Seq[A]](tableName, sql) { it: Iterator[A] =>
      it.toIndexedSeq
    }
  }

  def readStreamOf[A: ru.TypeTag, R](tableName: String, sql: String)(
      streamReader: scala.collection.Iterator[A] => R
  )(implicit conn: Connection): R = {
    val codec = MessageCodec.of[A]
    withResource(conn.createStatement()) { stmt =>
      debug(s"Executing query:\n${sql}")
      withResource(stmt.executeQuery(sql)) { rs =>
        val jdbcCodec = wvlet.airframe.codec.JDBCCodec(rs)
        val it = jdbcCodec.mapMsgPackArrayRows { msgpack =>
          codec.fromMsgPack(msgpack)
        }
        streamReader(it.toIterator)
      }
    }
  }
  //  def readAs[A: ru.TypeTag](rs: ResultSet): A = {
//    val cl = implicitly[ClassTag[A]].runtimeClass
//    val metadata = rs.getMetaData
//    val cols = metadata.getColumnCount
//    val b = ObjectBuilder(cl)
//    for (i <- 1 to cols) {
//      val colName = metadata.getColumnName(i)
//      b.set(colName, rs.getObject(i))
//    }
//    b.build.asInstanceOf[A]
//  }
}
