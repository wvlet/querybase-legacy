package wvlet.querybase.server.executor

import wvlet.airframe.Design
import wvlet.airframe.jdbc.DbConfig
import wvlet.airspec.AirSpec
import wvlet.querybase.server.executor.JobQueue.JobEntry
import wvlet.querybase.server.executor.presto.PrestoJob

/**
  */
class JobQueueTest extends AirSpec {

  override protected def design: Design = {
    JobQueue.design(JobQueueConfig(dbConfig = DbConfig.ofSQLite("target/job-queue.sqlite")))
  }

  test("add a job") { jobQueue: JobQueue =>
    val job = PrestoJob(catalog = "td-presto", sql = "select 1")
    val e   = JobEntry(jobType = "presto", jobData = job.toJson)
    jobQueue.add(e)
    val jobList = jobQueue.list
    info(jobList.mkString("\n"))
    jobQueue.updateState(e.withState(JobState.RUNNING))
    val jobList2 = jobQueue.list
    info(jobList2.mkString("\n"))
  }
}
