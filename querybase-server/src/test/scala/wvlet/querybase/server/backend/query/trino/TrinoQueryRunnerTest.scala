package wvlet.querybase.server.backend.query.trino

import wvlet.airframe.Design
import wvlet.airspec.AirSpec

/**
  */
class TrinoQueryRunnerTest extends AirSpec {

  if (sys.env.get("TD_API_KEY").isEmpty) {
    skip("No TD_API_KEY is found")
  }

  override protected def design: Design = TrinoQueryRunner.design

  test("Run Trino query") { queryRunner: TrinoQueryRunner =>
    val request = TrinoQueryRequest(
      "https://api-presto.treasuredata.com",
      user = sys.env("TD_API_KEY"),
      sql = "select * from sample_datasets.www_access limit 5",
      catalog = "td-presto"
    )

    val context = queryRunner.startQuery(request)
    context.run
    context.close()
  }

}
