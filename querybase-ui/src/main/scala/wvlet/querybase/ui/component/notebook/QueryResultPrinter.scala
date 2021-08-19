package wvlet.querybase.ui.component.notebook

import wvlet.querybase.api.backend.v1.CoordinatorApi.QueryResult

/** */
object QueryResultPrinter {

  def toTable(qi: QueryResult): Seq[Seq[String]] = {
    val rows = Seq.newBuilder[Seq[String]]
    rows += qi.schema.map(_.name).toIndexedSeq
    qi.rows.foreach { row =>
      val sanitizedRow = row.map { x =>
        Option(x).map(_.toString).getOrElse("")
      }
      rows += sanitizedRow.toSeq
    }
    rows.result()
  }

  def toTSV(qi: QueryResult): String = {
    toTable(qi).map(_.mkString("\t")).mkString("\n")
  }

  def print(qi: QueryResult): String = {
    val tbl = toTable(qi)

    val maxColSize: IndexedSeq[Int] =
      tbl
        .map { row =>
          row.map(_.size)
        }.reduce { (r1, r2) =>
          r1.zip(r2).map { case (l1, l2) =>
            l1.max(l2)
          }
        }.toIndexedSeq

    val header = tbl.head
    val data   = tbl.tail

    val rows = Seq.newBuilder[String]

    rows += header
      .zip(maxColSize).map { case (h, maxSize) =>
        h.padTo(maxSize, " ").mkString
      }.mkString(" | ")
    rows += maxColSize.map { s => "-" * s }.mkString("-+-")

    rows ++= data.map { row =>
      row
        .zip(maxColSize).map { case (x, maxSize) =>
          x.padTo(maxSize, " ").mkString
        }.mkString(" | ")
    }
    rows.result().mkString("\n")
  }

}
