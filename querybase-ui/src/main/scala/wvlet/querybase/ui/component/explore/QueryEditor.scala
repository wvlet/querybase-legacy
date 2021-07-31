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

  private val isIconVisible = Rx.variable(false)

  override def render: RxElement = MouseOverToggle(
    isIconVisible,
    table(
      tr(
        td(
        ),
        td(
          div(
            cls   -> "w-100",
            style -> "min-width: 500px;",
            new TextEditor("select 1")
          )
        )
      )
    )
  )
}
