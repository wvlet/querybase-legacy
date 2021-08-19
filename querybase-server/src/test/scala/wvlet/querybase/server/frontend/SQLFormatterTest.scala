package wvlet.querybase.server.frontend

import wvlet.airspec.AirSpec

/** */
class SQLFormatterTest extends AirSpec {

  test("format") {
    val text = SQLFormatter.format("select 1 from tbl group by 1")
    text.contains("\n") shouldBe true
  }

  test("format lambda operator") {
    val text = {
      SQLFormatter.format("""SELECT array_sort(ARRAY[3, 2, 5, 1, 2],(x, y) -> IF(x < y, 1, IF(x = y, 0, -1)))""")
    }
    text.contains("->") shouldBe true
  }

}
