package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.querybase.api.backend.v1.SearchApi.{SearchRequest, SearchResponse}

/**
  */
@RPC
trait SearchApi {
  def search(request: SearchRequest): SearchResponse

}

object SearchApi {
  // Search API
  case class SearchRequest(
      keyword: String
  )
  case class SearchResponse(
      results: Seq[SearchItem]
  )
  case class SearchItem(
      id: String,
      kind: String,
      title: String
  )
}
