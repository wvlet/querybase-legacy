package wvlet.querybase.server.frontend

import wvlet.airframe.codec.MessageCodecFactory
import wvlet.airframe.control.{Control, IO}
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.FrontendApi.{NotebookData, NotebookSession}

import java.io.{File, FileWriter}

/** */
class NotebookManager extends LogSupport {

  private val sessionStorePath = new File(".querybase", "sessions")
  sessionStorePath.mkdirs()
  private val notebookDataCodec = MessageCodecFactory.defaultFactoryForMapOutput.of[NotebookData]

  private def sessionFile(session: NotebookSession): File = {
    new File(sessionStorePath, session.id)
  }

  def saveNotebook(session: NotebookSession, data: NotebookData): Unit = {
    val json = notebookDataCodec.toJson(data)
    val file = sessionFile(session)
    Control.withResource(new FileWriter(file)) { out =>
      info(s"Saving the session to ${file}")
      out.write(json)
    }
  }

  def readNotebook(session: NotebookSession): Option[NotebookData] = {
    val file = sessionFile(session)
    if (file.exists()) {
      val json = IO.readAsString(file)
      try {
        val data = notebookDataCodec.fromJson(json)
        Some(data)
      } catch {
        case e: Throwable =>
          warn(e)
          None
      }
    } else {
      None
    }
  }
}
