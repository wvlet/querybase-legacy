package wvlet.querybase.ui.component.common

import scala.scalajs.js

/**
  */
object Clipboard {

  /** Write a text to the user's clipboard
    */
  def writeText(s: String): Unit = {
    js.Dynamic.global.navigator.clipboard.writeText(s)
  }

}
