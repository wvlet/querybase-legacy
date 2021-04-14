package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.{Cancelable, Rx}
import wvlet.querybase.api.frontend.FrontendApi.{NotebookCellData, NotebookData, NotebookSession, SaveNotebookRequest}
import wvlet.querybase.api.frontend.ServiceJSClientRx

import java.util.concurrent.TimeUnit

/**
  */
class NotebookSaver(notebookEditor: NotebookEditor, rpcRxClient: ServiceJSClientRx) {

  private var saveTimer: Option[Cancelable]    = None
  private var lastResult: Option[NotebookData] = None

  def start: Unit = {
    val saver = Rx
      .interval(3, TimeUnit.SECONDS).map { i =>
        notebookEditor.getNotebookData
      }.filter { data =>
        lastResult.isEmpty || lastResult.get != data
      }.map { data =>
        lastResult = Some(data)
        // save data
        rpcRxClient.FrontendApi.saveNotebook(SaveNotebookRequest(NotebookSession("default"), data))
        data
      }

    saveTimer.foreach { _.cancel }
    saveTimer = Some(saver.runContinuously[Unit] { x => })

  }

  def stop: Unit = {
    saveTimer.foreach(_.cancel)
  }
}
