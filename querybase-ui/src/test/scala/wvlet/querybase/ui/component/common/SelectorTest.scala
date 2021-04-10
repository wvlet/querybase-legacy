package wvlet.querybase.ui.component.common

import wvlet.airspec.AirSpec

/**
  */
class SelectorTest extends AirSpec {
  test("Selector") {
    val s = new Selector("selector", Seq(SelectorItem("label1", "l1"), SelectorItem("label2", "l2")))
    s.selectedIndex shouldBe 0
    s.selectAt(1)
    s.selectedIndex shouldBe 1
  }

  test("preserve selected index") {
    val i1 = SelectorItem("label1", "l1")
    val i2 = SelectorItem("label2", "l2")
    val s  = new Selector("selector", Seq(i1, i2))
    s.getSelectedItem shouldBe Some(i1)
    s.selectedIndex shouldBe 0

    s.selectAt(1)
    s.selectedIndex shouldBe 1
    s.getSelectedItem shouldBe Some(i2)

    val i3 = SelectorItem("label3", "l3")
    s.setItems(Seq(i1, i2, i3))
    // Preserve the previous selection even after the list is updated
    s.selectedIndex shouldBe 1
    s.getSelectedItem shouldBe Some(i2)

    s.selectAt(2)
    s.selectedIndex shouldBe 2
    s.getSelectedItem shouldBe Some(i3)
  }
}
