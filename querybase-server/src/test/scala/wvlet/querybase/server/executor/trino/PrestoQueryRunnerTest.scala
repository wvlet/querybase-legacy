package wvlet.querybase.server.executor.trino

import wvlet.airframe.Design
import wvlet.airspec.AirSpec

/**
  */
class PrestoQueryRunnerTest extends AirSpec {

  if (sys.env.get("TD_API_KEY").isEmpty) {
    skip("No TD_API_KEY is found")
  }

  override def design: Design = TrinoQueryRunner.design

  test("Run presto query") { queryRunner: TrinoQueryRunner =>
    val request = TrinoQueryRequest(
      "https://api-presto.treasuredata.com",
      user = sys.env("TD_API_KEY"),
      sql = "desc sample_datasets.www_access",
      catalog = "td-presto"
    )

    val context = queryRunner.startQuery(request)
    context.run
    context.close()
  }

}
