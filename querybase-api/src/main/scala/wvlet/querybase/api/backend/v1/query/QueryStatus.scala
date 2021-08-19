package wvlet.querybase.api.backend.v1.query

/** */
sealed trait QueryStatus {
  def isFinished: Boolean
}

object QueryStatus {

  case object STARTING extends QueryStatus {
    override def isFinished: Boolean = false
  }
  case object QUEUED extends QueryStatus {
    override def isFinished: Boolean = false
  }
  case object RUNNING extends QueryStatus {
    override def isFinished: Boolean = false
  }
  case object FINISHED extends QueryStatus {
    override def isFinished: Boolean = true
  }
  case object FAILED extends QueryStatus {
    override def isFinished: Boolean = true
  }
  case object CANCELED extends QueryStatus {
    override def isFinished: Boolean = true
  }
  case object UNKNOWN extends QueryStatus {
    override def isFinished: Boolean = true
  }

  private def statusTable =
    Seq(STARTING, QUEUED, RUNNING, FINISHED, FAILED, CANCELED, UNKNOWN).map { x =>
      x.toString -> x
    }.toMap

  def unapply(s: String): Option[QueryStatus] = {
    Some(statusTable.getOrElse(s.toUpperCase(), UNKNOWN))
  }
}
