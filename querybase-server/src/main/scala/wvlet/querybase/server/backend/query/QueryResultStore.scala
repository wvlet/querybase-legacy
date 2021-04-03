package wvlet.querybase.server.backend.query

import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryId

import java.io.File

/**
  */
class QueryResultStore(queryExecutorConfig: QueryExecutorConfig) {

  init

  private def init: Unit = {
    if (!queryExecutorConfig.queryResultStorePath.exists()) {
      queryExecutorConfig.queryResultStorePath.mkdirs()
    }
  }

  def createNewResultFile(queryId: QueryId): File = {
    val queryResultFile = getResultFile(queryId)
    queryResultFile.getParentFile.mkdirs()
    queryResultFile
  }

  def getResultFile(queryId: QueryId): File = {
    // Query folder .querybase/results/yyyyMMdd-HH/(query id)
    val folder          = queryId.substring(0, 11.min(queryId.size))
    val queryResultDir  = new File(new File(queryExecutorConfig.queryResultStorePath, folder), queryId)
    val queryResultFile = new File(queryResultDir, "result.msgpack.snappy")
    queryResultFile
  }
}
