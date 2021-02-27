package wvlet.querybase.api.backend.v1.query

import java.util.Locale

/**
  */
sealed trait QueryStatus

object QueryStatus {

  case object QUEUED   extends QueryStatus
  case object RUNNING  extends QueryStatus
  case object FINISHED extends QueryStatus
  case object FAILED   extends QueryStatus
  case object CANCELED extends QueryStatus
  case object UNKNOWN  extends QueryStatus

  private def statusTable =
    Seq(QUEUED, RUNNING, FINISHED, FAILED, CANCELED, UNKNOWN).map { x =>
      x.toString -> x
    }.toMap

  def unapply(s: String): Option[QueryStatus] = {
    Some(statusTable.getOrElse(s.toUpperCase(Locale.US), UNKNOWN))
  }
}
