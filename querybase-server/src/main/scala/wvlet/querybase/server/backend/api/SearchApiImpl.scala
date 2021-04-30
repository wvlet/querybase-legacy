package wvlet.querybase.server.backend.api

import wvlet.airframe.ulid.ULID
import wvlet.querybase.api.backend.v1.SearchApi
import wvlet.querybase.api.backend.v1.SearchApi.{SearchItem, SearchResponse}
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

/**
  */
class SearchApiImpl(coordinatorClient: CoordinatorClient) extends SearchApi {

  private def searchHistory: Unit = {}

  override def search(request: SearchApi.SearchRequest): SearchApi.SearchResponse = {
    // dummy response
    val services = coordinatorClient.v1.ServiceCatalogApi.listServices().map { x =>
      SearchItem(id = ULID.newULIDString, kind = "service", title = x.name)
    }

    val tables = Seq(
      SearchItem(
        id = ULID.newULIDString,
        kind = "table",
        title = "query_completion"
      ),
      SearchItem(
        id = ULID.newULIDString,
        kind = "table",
        title = "accounts"
      )
    )

    val queries = Seq(
      SearchItem(
        id = ULID.newULIDString,
        kind = "query",
        title = "Account list"
      )
    )

    val notebooks = Seq(
      SearchItem(
        id = ULID.newULIDString,
        kind = "notebook",
        title = "My Notebook"
      )
    )

    val list        = services ++ tables ++ queries ++ notebooks
    val matchedList = list.filter(_.title.toLowerCase.contains(request.keyword))

    val results = Seq.newBuilder[SearchItem]
    if (request.keyword.nonEmpty) {
      results += SearchItem(id = ULID.newULIDString, kind = "search", title = request.keyword)
    }
    results ++= matchedList

    SearchResponse(
      results.result()
    )
  }

}
