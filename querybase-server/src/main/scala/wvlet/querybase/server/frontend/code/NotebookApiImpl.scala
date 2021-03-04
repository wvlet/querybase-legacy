package wvlet.querybase.server.frontend.code

import java.time.Instant

import wvlet.querybase.api.frontend.code.NotebookApi
import NotebookApi._
import wvlet.airframe.control.ULID

/**
  */
class NotebookApiImpl extends NotebookApi {
  override def createNotebook(notebook: Notebook, requestId: ULID): Option[Notebook] = None

  override def getNotebook(id: String): Option[Notebook] = {
    Some(
      Notebook(
        id = "1",
        name = "my notebook",
        description = "Example notebook",
        createdAt = Instant.parse("2020-11-01T01:23:45.000Z"),
        updatedAt = Instant.now(),
        cells = Seq(
          Cell(
            cellType = "sql",
            source = """-- Simple SQL
                      |select 100
                      |""".stripMargin,
            outputs = Seq(
              """{"output_type":"stream", "name":"stdout", "text":"(query result)\n1"}"""
            )
          ),
          Cell(
            cellType = "sql",
            source = "show tables from sample_datasets"
          )
        )
      )
    )
  }

  override def updateNotebook(notebook: Notebook): Unit = {}

  override def deleteNotebook(notebookId: String, requestId: ULID): Unit = {}
}
