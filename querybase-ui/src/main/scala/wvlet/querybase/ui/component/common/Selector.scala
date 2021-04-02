package wvlet.querybase.ui.component.common

import org.scalajs.dom.raw.{Event, HTMLSelectElement}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

case class SelectorItem(name: String, value: String)

/**
  */
class Selector(name: String, items: Seq[SelectorItem]) extends RxElement {
  private var selectedIndex: Int = 0

  def getSelectedIndex: Int = selectedIndex

  def selectAt(index: Int): Unit = {
    val maxIndex = (items.size - 1).max(0)
    selectedIndex = index.min(maxIndex)
  }

  override def render: RxElement = {
    form(
      cls -> "form-inline",
      div(
        cls -> "form-group",
        label(cls -> "mr-2", small(name)),
        div(
          select(
            onchange -> { e: Event =>
              e.target match {
                case e: HTMLSelectElement =>
                  selectedIndex = e.selectedIndex
                case _ =>
              }
            },
            cls -> "form-control form-control-sm",
            items.map { x =>
              option(
                value -> x.value,
                x.name
              )
            }
          )
        )
      )
    )
  }
}
