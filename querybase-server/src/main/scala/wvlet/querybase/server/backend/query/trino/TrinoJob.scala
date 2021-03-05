package wvlet.querybase.server.backend.query.trino

import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.json.Json

/**
  */
case class TrinoJob(
    catalog: String,
    sql: String
) {
  def toJson: Json = {
    MessageCodec.of[TrinoJob].toJson(this)
  }
}

object TrinoJob {}
