package wvlet.querybase.server.backend.search

/** */
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.control.Control.withResource
import wvlet.airframe.surface.{OptionSurface, Primitive, Surface}
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport

import java.sql.Connection

/** */
object SQLHelper extends LogSupport {

  import scala.reflect.runtime.{universe => ru}

  def sqlTypeOf(tpe: Surface): String = {
    tpe match {
      case Primitive.Int     => "integer"
      case Primitive.Short   => "integer"
      case Primitive.Long    => "integer"
      case Primitive.Float   => "float"
      case Primitive.Double  => "float"
      case Primitive.Boolean => "boolean"
      case Primitive.String  => "string"
      case _                 => "string"
    }
  }

  def createTableSQLFor[A: ru.TypeTag](
      tableName: String,
      paramOption: Map[String, String] = Map.empty,
      additionalParams: Seq[String] = Seq.empty
  ): String = {
    val surface = Surface.of[A]
    val params = for (p <- surface.params) yield {
      val colDef = s"${p.name} ${sqlTypeOf(p.surface)}"
      paramOption.get(p.name) match {
        case Some(opts) => s"${colDef} ${opts}"
        case None       => colDef
      }
    }
    s"create table if not exists ${tableName} (${(params ++ additionalParams).mkString(", ")})"
  }

  def quote(s: String): String = s"'${s}'"

  private def convert(v: Any, surface: Surface): Any = {
    v match {
      case m: Map[_, _] =>
        if (m.isEmpty) {
          null
        } else {
          MessageCodec.of[Map[String, Any]].toJson(m.asInstanceOf[Map[String, Any]])
        }
      case ulid if surface.rawType == classOf[ULID] =>
        ulid.toString
      case obj if !surface.isPrimitive && surface.params.nonEmpty =>
        MessageCodec.ofSurface(surface).asInstanceOf[MessageCodec[Any]].toJson(obj)
      case _ => v
    }
  }

  def insertRecord[A: ru.TypeTag](tableName: String, obj: A)(implicit conn: Connection): Unit = {
    val surface = Surface.of[A]
    val colSize = surface.params.size
    val tuple   = ("?" * colSize).mkString(", ")
    withResource(conn.prepareStatement(s"insert into ${tableName} values(${tuple})")) { prep =>
      for (p <- surface.params) yield {
        val v = p.get(obj)
        p.surface match {
          case o: OptionSurface =>
            v match {
              case Some(x) if x != null =>
                prep.setObject(p.index + 1, convert(x, o.elementSurface))
              case _ =>
                prep.setNull(p.index + 1, java.sql.Types.NULL)
            }
          case _ =>
            convert(v, p.surface) match {
              case null =>
                prep.setNull(p.index + 1, java.sql.Types.NULL)
              case x =>
                prep.setObject(p.index + 1, x)
            }
        }
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
  ): Unit = {
    val surface = Surface.of[A]
    val updateColumns = columnMask
      .map { column =>
        s""""${column}" = ?"""
      }.mkString(", ")

    val sql = s"""update "${tableName}" set ${updateColumns} where "${idColumn}" = ?"""
    debug(sql)

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

  def readAs[A: ru.TypeTag](sql: String)(implicit conn: Connection): Seq[A] = {
    readStreamOf[A, Seq[A]](sql) { it: Iterator[A] =>
      it.toIndexedSeq
    }
  }

  def readStreamOf[A: ru.TypeTag, R](sql: String)(
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
        streamReader(it.iterator)
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
