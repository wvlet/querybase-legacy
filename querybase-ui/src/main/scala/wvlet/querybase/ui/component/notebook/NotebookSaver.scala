package wvlet.querybase.ui.component.notebook

import wvlet.airframe.rx.{Cancelable, Rx}
import wvlet.querybase.api.frontend.FrontendApi.{NotebookCellData, NotebookData, NotebookSession, SaveNotebookRequest}
import wvlet.querybase.api.frontend.{ServiceJSClient, ServiceJSClientRx}

import java.util.concurrent.TimeUnit

/** */
class NotebookSaver(notebookEditor: NotebookEditor, rpcClient: ServiceJSClient) {

  private var saveTimer: Option[Cancelable]    = None
  private var lastResult: Option[NotebookData] = None

  def start: Unit = {
    val saver =
      Rx
        .interval(1, TimeUnit.SECONDS)
        .map { i =>
          val data = notebookEditor.getNotebookData
          save(data)
          data
        }

    saveTimer.foreach { _.cancel }
    saveTimer = Some(saver.runContinuously[Unit] { x => })
  }

  def save(data: NotebookData): Unit = {
    if (lastResult.isEmpty || lastResult.get != data) {
      lastResult = Some(data)
      rpcClient.FrontendApi.saveNotebook(SaveNotebookRequest(notebookEditor.currentSession, data))
    }
  }

  def stop: Unit = {
    saveTimer.foreach(_.cancel)
  }
}
