package wvlet.querybase.server.api.code

import wvlet.querybase.api.v1.code.NotebookApi
import NotebookApi._
import wvlet.airframe.control.ULID

/**
  */
class NotebookApiImpl extends NotebookApi {
  override def createNotebook(notebook: Notebook, requestId: ULID): Option[Notebook] = None

  override def getNotebook(id: String): Option[Notebook] = None

  override def updateNotebook(notebook: Notebook): Unit = {}

  override def deleteNotebook(notebookId: String, requestId: ULID): Unit = {}
}
