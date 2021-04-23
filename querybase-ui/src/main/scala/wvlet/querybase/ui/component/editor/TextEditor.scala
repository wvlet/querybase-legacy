package wvlet.querybase.ui.component.editor

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import wvlet.airframe.ulid.ULID
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all.{cls, div, style}
import wvlet.log.LogSupport
import wvlet.airframe.rx.html.widget.editor.monaco.editor.Editor.IEditorModel
import wvlet.airframe.rx.html.widget.editor.monaco.{
  CancellationToken,
  IKeyboardEvent,
  KeyCode,
  Monaco,
  Position,
  Range,
  editor
}
import wvlet.airframe.rx.html.widget.editor.monaco.editor.{
  Editor,
  IDimension,
  IEditorMinimapOptions,
  IEditorScrollbarOptions,
  IIdentifiedSingleEditOperation,
  IModelDecorationsChangedEvent,
  IStandaloneEditorConstructionOptions,
  IStandaloneThemeData,
  ITextModel,
  ITokenThemeRule,
  RenderLineNumbersType
}
import wvlet.airframe.rx.html.widget.editor.monaco.languages.Languages.{ProviderResult, TextEdit}
import wvlet.airframe.rx.html.widget.editor.monaco.languages.{
  DocumentFormattingEditProvider,
  FormattingOptions,
  Languages
}

import scala.scalajs.js
import scala.scalajs.js.Dictionary
import js.JSConverters._

/**
  */
class TextEditor(
    initialValue: String = "",
    maxHeight: Int = 300,
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
    scrollbarOptions.handleMouseWheel = true
    scrollbarOptions.alwaysConsumeMouseWheel = false
    option.scrollbar = scrollbarOptions

    // Code formatter
    //  option.formatOnType = true
    val editor = Editor.create(editorNode, option)
    TextEditor.init

    editor.onKeyDown { e: IKeyboardEvent =>
      if (e.keyCode == KeyCode.Enter && (e.metaKey || e.ctrlKey)) {
        val text = editor.getValue()
        debug(s"Command + Enter is pressed:\n[content]\n${text}")
        onEnter(text)
        e.stopPropagation()
      }

      if (e.keyCode == KeyCode.UpArrow || e.keyCode == KeyCode.PageUp) {
        if (cursorPosition.lineNumber == 1) {
          //debug("Exit up from the editor")
          onExitUp()
        }
      }

      if (e.keyCode == KeyCode.DownArrow || e.keyCode == KeyCode.PageDown) {
        if (cursorPosition.lineNumber == lineCount) {
          //debug(s"Exit from the editor")
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
  def setTextValue(text: String): Unit = {
    // Use ExecuteEdits to support Undo
    editor.executeEdits(
      "editor",
      Array(
        js.Dynamic
          .literal(
            range = editor.getModel().asInstanceOf[ITextModel].getFullModelRange(),
            text = text,
            forceMoveMarkers = true
          ).asInstanceOf[IIdentifiedSingleEditOperation]
      ).toJSArray
    )
    updateLayout()
  }
  def undo: Unit = {
    editor.trigger("whatever...", "undo", "undo")

  }
  def redo: Unit = {
    editor.trigger("editor", "redo", null)
  }

  def formatCode: Unit = {
    info(s"format code:\n${getTextValue}")
    editor.getAction("editor.action.formatDocument").run()
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

    scalajs.js.timers.setTimeout(50) {
      updateLayout()
    }
    //updateLayout(isInit = true)
    div(
      cls   -> "py-1",
      style -> "background: #202124;",
      editorNode
    )
  }
}

object TextEditor extends LogSupport {
  {
    val editorTheme = new js.Object().asInstanceOf[IStandaloneThemeData]
    editorTheme.base = "vs-dark"
    editorTheme.inherit = true

    def newRule(f: ITokenThemeRule => Unit): ITokenThemeRule = {
      val r = new js.Object().asInstanceOf[ITokenThemeRule]
      f(r)
      r
    }

    val rules = Seq[ITokenThemeRule](
      newRule { r =>
        r.token = "keyword"
        r.foreground = "#c9c6fc"
      },
      newRule { r =>
        r.token = "number.sql"
        r.foreground = "#bec5ce"
      },
      newRule { r =>
        r.token = "string.sql"
        r.foreground = "#f4c099"
      },
      newRule { r =>
        // pre-defined SQL functions
        r.token = "predefined.sql"
        r.foreground = "#58ccf0"
      },
      newRule { r =>
        // Double quotation
        r.token = "identifier"
        r.foreground = "#ffffff"
      },
      newRule { r =>
        // Inside double quotation, $VAR
        r.token = "identifier.sql"
        r.foreground = "#eeeeee"
      },
      newRule { r =>
        r.token = "comment"
        r.foreground = "#99cc99"
      }
    ).toJSArray
    editorTheme.rules = rules
    editorTheme.colors = Dictionary[String](
      ("editor.background", "#202124")
    )
    Editor.defineTheme("vs-querybase", editorTheme)
  }

  private def init: Boolean = {
    js.Dynamic.global.monaco.languages.registerDocumentFormattingEditProvider(
      "sql",
      js.Dynamic.literal(
        provideDocumentFormattingEdits = {
          (model: ITextModel, range: Range, options: FormattingOptions, token: CancellationToken) =>
            val textValue = model.getValue()
            val fullRange = model.getFullModelRange()
            info(s"format code: ${textValue} ${fullRange.startLineNumber}, ${fullRange.endLineNumber}")
            Array[TextEdit](
              js.Dynamic.literal(
                range = model.getFullModelRange(),
                // This should be formatted
                text = textValue
              )
            ).toJSArray
        }
      )
    )

    true
  }
}
