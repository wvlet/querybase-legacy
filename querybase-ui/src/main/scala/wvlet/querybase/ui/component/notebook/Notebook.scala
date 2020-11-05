package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.querybase.ui.component.editor.TextEditor

/**
  */
class Notebook extends RxElement {
  override def render: RxElement = {
    div(
      div("notebook"),
      new TextEditor("select 1")
    )
  }
}
