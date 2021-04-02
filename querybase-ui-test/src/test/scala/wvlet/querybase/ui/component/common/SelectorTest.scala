package wvlet.querybase.ui.component.common

import wvlet.airspec.AirSpec

/**
  */
class SelectorTest extends AirSpec {
  test("Selector") {
    val s = new Selector("selector", Seq(SelectorItem("label1", "l1"), SelectorItem("label2", "l2")))
    s.getSelectedIndex shouldBe 0
    s.selectAt(1)
    s.getSelectedIndex shouldBe 1
  }
}
