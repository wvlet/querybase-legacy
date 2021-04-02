package wvlet.querybase.server.backend.query

import org.xerial.snappy.SnappyInputStream
import wvlet.airframe.codec.{MessageCodec, MessageContext}
import wvlet.airframe.control.Control.withResource
import wvlet.airframe.msgpack.spi.MessagePack
import wvlet.log.LogSupport
import wvlet.querybase.api.backend.v1.CoordinatorApi.{Column, QueryResult}

import java.io.{File, FileInputStream}

/**
  */
class QueryResultFileReader(file: File) extends LogSupport {
  assert(file.getName.endsWith(".msgpack.snappy"), s"unsupported file format: ${file}")

  private val schemaCodec = MessageCodec.of[Seq[Column]]
  private val resultCodec = MessageCodec.of[Seq[Any]]

  def readRows(limit: Int): QueryResult = {
    withResource(MessagePack.newUnpacker(new SnappyInputStream(new FileInputStream(file)))) { unpacker =>
      val schemaValue = unpacker.unpackValue
      val schema      = schemaCodec.fromMsgPack(schemaValue.toMsgpack)

      val rows      = Seq.newBuilder[Seq[Any]]
      val ctx       = new MessageContext()
      var readCount = 0
      while (unpacker.hasNext && readCount <= limit) {
        resultCodec.unpack(unpacker, ctx)
        ctx.getLastValue match {
          case row: Seq[Any] =>
            rows += row
            readCount += 1
          case _ =>
        }
      }
      QueryResult(schema, rows.result())
    }
  }

}
