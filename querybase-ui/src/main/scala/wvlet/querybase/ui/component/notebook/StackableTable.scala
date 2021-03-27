package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

/**
  */
class StackableTable(lst: Seq[RxElement]) extends RxElement {

  def updateRows(newList: Seq[RxElement]): Unit = {}

  def focusOnCell(cellIndex: Int): Unit = {}

  def focusOrCreateCell(cellIndex: Int, builder: () => RxElement): Unit = {}

  override def render: RxElement = div(
    cls -> "w-100",
    table(
      lst.zipWithIndex.map { case (x, i) =>
        tr(
          id -> s"cell-${i}",
          td(
            x
          )
        )
      }
    )
  )

  class StackElement(elem: RxElement) extends RxElement {

    def focus: Unit = {}

    def unfocus: Unit = {}

    override def render: RxElement = {
      tr(
        id -> s"cell-${i}",
        td(
          elem
        )
      )
    }

  }

}
