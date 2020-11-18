package wvlet.querybase.server.executor.presto

import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.json.Json

/**
  */
case class PrestoJob(
    catalog: String,
    sql: String
) {
  def toJson: Json = {
    MessageCodec.of[PrestoJob].toJson(this)
  }
}

object PrestoJob {}
