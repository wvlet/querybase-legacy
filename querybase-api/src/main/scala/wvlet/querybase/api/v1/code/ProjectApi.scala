package wvlet.querybase.api.v1.code

import ProjectApi._
import java.time.Instant
import wvlet.airframe.http.RPC
import wvlet.airframe.control.ULID
import java.{util => ju}

@RPC
trait ProjectApi {
  def listProject: Seq[Project]
  def getProject(id: String): Option[Project]
  def updateProject(project: Project, requestId: ULID): Option[Project]
  def createProject(project: Project, requestId: ju.UUID): Option[Project]
  def deleteProject(projectId: String, requestId: ULID): Unit
}

object ProjectApi {
  case class Project(
      // The server-generated project ID
      id: String = "N/A",
      // The name for referencing this project
      name: String,
      // The description of the project
      description: String,
      // The time when the project was created
      createdAt: Instant = Instant.now()
  )
}
