package wvlet.querybase.server.executor

/** */
sealed trait JobState
object JobState {
  def all = Seq(QUEUED, RUNNING, FINISHED, FAILED, CANCELLED)

  def unapply(s: String): Option[JobState] = {
    all.find(_.toString == s)
  }

  case object QUEUED    extends JobState
  case object RUNNING   extends JobState
  case object FINISHED  extends JobState
  case object FAILED    extends JobState
  case object CANCELLED extends JobState
}
