package wvlet.querybase.ui.component.explore

import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.common.{HStack, HiddenElem, LabeledForm, MouseOverToggle, VStack}
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.querybase.ui.component.notebook.EditorIcon

/**
  */
class QueryEditor extends RxElement {

  private val focusOnEditor = Rx.variable(false)
  private val textEditor    = new TextEditor("select 1")

  def setText(text: String): Unit = {
    textEditor.setTextValue(text)
  }

  override def render: RxElement = {
    scala.scalajs.js.timers.setTimeout(100) {
      textEditor.focus
    }
    table(
      cls -> "w-100",
      tr(
        MouseOverToggle(focusOnEditor),
        td(
          // Put the icon at the top of the cell
          cls -> "align-top text-center bg-light",
          HiddenElem(focusOnEditor, new EditorIcon("Add new cell", "fa-plus"))
        ),
        td(
          cls -> "align-middle",
          //style -> "display: flex; flex-direction: column; position: relative; ",
          textEditor
        )
      )
    )
  }

}
