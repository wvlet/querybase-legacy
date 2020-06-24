package wvlet.querybase.api.v1.query

import wvlet.airframe.http.RPC

/**
  */
@RPC
trait QueryApi {
  import QueryApi._

  def list: Seq[Query]
}

object QueryApi {

  case class Query(query: String)

}
