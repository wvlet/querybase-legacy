package wvlet.querybase.ui.component.editor

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import wvlet.airframe.control.ULID
import wvlet.airframe.rx.html.RxElement
import wvlet.log.LogSupport
import wvlet.airframe.rx.html.widget.editor.monaco.editor.Editor.IEditorModel
import wvlet.airframe.rx.html.widget.editor.monaco.{IKeyboardEvent, KeyCode, Position}
import wvlet.airframe.rx.html.widget.editor.monaco.editor.{
  Editor,
  IDimension,
  IEditorMinimapOptions,
  IEditorScrollbarOptions,
  IModelDecorationsChangedEvent,
  IStandaloneEditorConstructionOptions,
  IStandaloneThemeData,
  ITextModel,
  ITokenThemeRule,
  RenderLineNumbersType
}

import scala.scalajs.js

/**
  */
class TextEditor(
    initialValue: String = "",
    maxHeight: Int = 250,
    onEnter: String => Unit = { x: String => },
    onExitUp: () => Unit = { () => },
    onExitDown: () => Unit = { () => }
) extends RxElement
    with LogSupport {

  private val editorId = ULID.newULID.toString()

  private val editorNode = {
    val editorNode: HTMLElement = document.createElement("div").asInstanceOf[HTMLElement]
    editorNode.setAttribute("id", editorId)
    editorNode.setAttribute("class", "query-editor")
    editorNode.setAttribute("style", s"width: 99%; min-height: 18px; max-height: ${maxHeight}px;")
    editorNode
  }

  private val editor = {
    val editorTheme = new js.Object().asInstanceOf[IStandaloneThemeData]
    editorTheme.base = "vs-dark"
    editorTheme.inherit = true

    import js.JSConverters._

    def newRule = new js.Object().asInstanceOf[ITokenThemeRule]

    val r1 = newRule
    r1.token = "keyword"
    r1.foreground = "#aa99dd"
//    val r2 = newRule
//    r2.token = "string"
//    r2.foreground = "#eeeeee"
    val rules = Seq[ITokenThemeRule](
      r1
//      r2
    ).toJSArray
    editorTheme.rules = rules
    Editor.defineTheme("vs-querybase", editorTheme)

    val option = new scalajs.js.Object().asInstanceOf[IStandaloneEditorConstructionOptions]
    option.value = initialValue
    option.language = "sql"
    option.theme = "vs-querybase" // "vs-dark"
    option.lineNumbers = "on"
    option.renderLineHighlight = "none"
    option.glyphMargin = false
    option.wordWrap = "on"
    option.folding = false
    option.dragAndDrop = true
    option.renderIndentGuides = true
    option.tabSize = 2.0
    option.lineDecorationsWidth = 10
    option.scrollBeyondLastLine = false
    option.lineHeight = 18
    option.automaticLayout = true
    option.fontFamily = "Menlo, Monaco, Consolas, Liberation Mono, Courier New, monospace"
    option.fontSize = 12
    option.fontLigatures = true
    option.fixedOverflowWidgets = true
    option.contextmenu = false
    val minimapOptions = new js.Object().asInstanceOf[IEditorMinimapOptions]
    minimapOptions.enabled = false
    option.minimap = minimapOptions

    val scrollbarOptions = new js.Object().asInstanceOf[IEditorScrollbarOptions]
    scrollbarOptions.alwaysConsumeMouseWheel = true
    option.scrollbar = scrollbarOptions

    val editor = Editor.create(editorNode, option)
    editor.onKeyDown { e: IKeyboardEvent =>
      if (e.keyCode == KeyCode.Enter && (e.metaKey || e.ctrlKey)) {
        val text = editor.getValue()
        debug(s"Command + Enter is pressed:\n[content]\n${text}")
        onEnter(text)
        e.stopPropagation()
      }

      if (e.keyCode == KeyCode.UpArrow || e.keyCode == KeyCode.PageUp) {
        if (cursorPosition.lineNumber == 1) {
          debug("Exit up from the editor")
          onExitUp()
        }
      }

      if (e.keyCode == KeyCode.DownArrow || e.keyCode == KeyCode.PageDown) {
        if (cursorPosition.lineNumber == lineCount) {
          debug(s"Exit from the editor")
          onExitDown()
        }
      }
    }
    editor
  }

  def focus: Unit = {
    editor.focus()
  }

  def cursorPosition: Position = {
    editor.getPosition().asInstanceOf[Position]
  }

  def lineCount: Int = {
    editor.getModel().asInstanceOf[ITextModel].getLineCount().toInt
  }

  def getTextValue: String = {
    editor.getValue()
  }

  def updateLayout(isInit: Boolean = false): Unit = {
    val lineHeight  = editor.getRawOptions().lineHeight
    val lineCount   = editor.getModel().asInstanceOf[ITextModel].getLineCount()
    val minHeight   = lineCount * lineHeight
    val lastLinePos = editor.getTopForLineNumber(lineCount + 1)

    val height = (if (isInit) lineCount * lineHeight else (lastLinePos + lineHeight)).max(minHeight).min(maxHeight)
    editor.getDomNode() match {
      case el: HTMLElement =>
        el.style.height = s"${height}px"
        editor.layout()
      case _ =>
    }
  }

  override def render: RxElement = {
    editor.onDidChangeModelDecorations { e: IModelDecorationsChangedEvent =>
      updateLayout()
    }

    //updateLayout(isInit = true)
    editorNode
  }
}
