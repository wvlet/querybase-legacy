package wvlet.querybase.ui.component.common

import org.scalajs.dom.raw.{Event, HTMLSelectElement}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._

case class SelectorItem(name: String, value: String)

/**
  */
class Selector(name: String, private var items: Seq[SelectorItem]) extends RxElement {
  private var _selectedIndex: Int = 0

  def selectedIndex: Int = _selectedIndex
  def getSelectedItem: Option[SelectorItem] = {
    if (_selectedIndex >= 0 && _selectedIndex < items.size) {
      Some(items(_selectedIndex))
    } else {
      None
    }
  }

  def setItems(newItems: Seq[SelectorItem]): Unit = {
    getSelectedItem match {
      case Some(selectedItem) =>
        // Select the same item after reload
        newItems.zipWithIndex.find(_._1 == selectedItem).foreach { x =>
          _selectedIndex = x._2
        }
      case None =>
        _selectedIndex = 0
    }
    items = newItems
  }

  def selectAt(index: Int): Unit = {
    val maxIndex = (items.size - 1).max(0)
    _selectedIndex = index.min(maxIndex)
  }

  override def render: RxElement = {
    form(
      cls -> "form-inline",
      div(
        cls -> "form-group",
        label(cls -> "mr-2", small(s"${name}:")),
        div(
          select(
            onchange -> { e: Event =>
              e.target match {
                case e: HTMLSelectElement =>
                  _selectedIndex = e.selectedIndex
                case _ =>
              }
            },
            cls -> "form-control form-control-sm",
            items.zipWithIndex.map { case (x, i) =>
              option(
                selected.when(_selectedIndex == i),
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
