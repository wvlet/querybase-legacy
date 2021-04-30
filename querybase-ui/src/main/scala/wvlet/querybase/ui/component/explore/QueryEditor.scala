package wvlet.querybase.ui.component.explore

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.common.{HStack, LabeledForm, VStack}
import wvlet.querybase.ui.component.editor.TextEditor
import wvlet.querybase.ui.component.notebook.EditorIcon

/**
  */
class QueryEditor extends RxElement {

  private val form = LabeledForm().withLabel("$var").withPlaceholder("value").withSmallSize

  private val isIconVisible = Rx.variable(true)

  override def render: RxElement = {
    div(
      onmouseover -> { e: MouseEvent => isIconVisible := true },
      onmouseout  -> { e: MouseEvent => isIconVisible := false },
      table(
        tr(
          td(
            isIconVisible.map { x =>
              style -> s"visibility: ${if (x) "visible" else "hidden"};"
            },
            new EditorIcon("New variable", "fa-plus")
          ),
          td(
            form
          )
        ),
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
}
