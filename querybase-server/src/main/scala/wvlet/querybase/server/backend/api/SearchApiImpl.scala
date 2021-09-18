package wvlet.querybase.server.backend.api

import wvlet.airframe.ulid.ULID
import wvlet.querybase.api.backend.v1.{SearchApi, ServiceCatalogApi}
import wvlet.querybase.api.backend.v1.SearchApi.{SearchItem, SearchResponse}

/** */
class SearchApiImpl(serviceCatalogApi: ServiceCatalogApi) extends SearchApi {

  private def searchHistory: Unit = {}

  override def search(request: SearchApi.SearchRequest): SearchApi.SearchResponse = {
    // dummy response
    val services = serviceCatalogApi.listServices.map { x =>
      SearchItem(id = ULID.newULIDString, kind = "service", title = x.name)
    }

    val databases = Seq(
      SearchItem(
        id = ULID.newULIDString,
        kind = "database",
        title = "summary"
      )
    )

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

    val list        = databases ++ services ++ tables ++ queries ++ notebooks
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
