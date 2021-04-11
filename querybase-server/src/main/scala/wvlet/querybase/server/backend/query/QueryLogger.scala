package wvlet.querybase.server.backend.query

import wvlet.airframe.codec.{MessageCodec, MessageCodecFactory}
import wvlet.log.{AsyncHandler, LogFormatter, LogLevel, LogRecord, LogRotationHandler, LogSupport, Logger}
import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryInfo
import wvlet.querybase.server.backend.query.QueryLogger.QueryLoggerFormatter

import java.io.File

class QueryLogger {

  private val queryStartLogger      = Logger("querylogger.start")
  private val queryCompletionLogger = Logger("querylogger.completion")

  private val queryHistoryDir = new File(".querybase/history")

  init

  private def init: Unit = {
    queryHistoryDir.mkdirs()

    queryStartLogger.setLogLevel(LogLevel.ALL)
    queryCompletionLogger.setLogLevel(LogLevel.ALL)

    queryStartLogger.resetHandler(
      new LogRotationHandler(
        fileName = ".querybase/history/query_start.json",
        formatter = QueryLoggerFormatter,
        logFileExt = ".json"
      )
    )
    queryCompletionLogger.resetHandler(
      new LogRotationHandler(
        fileName = ".querybase/history/query_completion.json",
        formatter = QueryLoggerFormatter,
        logFileExt = ".json"
      )
    )
  }

  private val codec = MessageCodecFactory.defaultFactoryForJSON.of[QueryInfo]

  def startLog(queryInfo: QueryInfo): Unit = {
    queryStartLogger.info(codec.toJson(queryInfo))
  }
  def completionLog(queryInfo: QueryInfo): Unit = {
    queryCompletionLogger.info(codec.toJson(queryInfo))
  }
}

object QueryLogger {
  object QueryLoggerFormatter extends LogFormatter {
    override def formatLog(r: LogRecord): String = {
      r.getMessage
    }
  }

}
