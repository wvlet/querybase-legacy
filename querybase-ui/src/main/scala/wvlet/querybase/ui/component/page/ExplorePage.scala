package wvlet.querybase.ui.component.page

import wvlet.airframe._
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div}
import wvlet.querybase.ui.component.notebook.NotebookFrame

/**
  */
trait ExplorePage extends RxElement {
  private val notebookFrame = bind[NotebookFrame]

  override def render: RxElement = div(
    cls -> "p-1 w-100",
    notebookFrame
  )
}
