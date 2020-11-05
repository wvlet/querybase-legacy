package wvlet.querybase.api.v1.code

import java.time.Instant

import wvlet.airframe.control.ULID
import wvlet.airframe.http.RPC

object NotebookApi {

  case class Notebook(
      // The server-generated ID
      id: String = "N/A",
      // The note book name
      name: String,
      // The description of the notebook
      description: String,
      // The time when the notebook was created
      createdAt: Instant = Instant.now(),
      // The time when the notebook was updated
      updatedAt: Instant = Instant.now(),
      // Notebook cells
      cells: Seq[Cell] = Seq.empty
  ) {
    def withCells(newCells: Seq[Cell]): Notebook = {
      this.copy(cells = newCells, updatedAt = Instant.now())
    }
  }

  case class Cell(
      cellType: String,
      // Text source
      source: String
  )

}

/**
  */
@RPC
trait NotebookApi {

  import NotebookApi._

  def createNotebook(notebook: Notebook, requestId: ULID): Option[Notebook]
  def getNotebook(id: String): Option[Notebook]
  def updateNotebook(notebook: Notebook): Unit
  def deleteNotebook(notebookId: String, requestId: ULID): Unit
}
