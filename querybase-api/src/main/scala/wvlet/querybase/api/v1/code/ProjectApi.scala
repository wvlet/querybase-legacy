package wvlet.querybase.api.v1.code

import ProjectApi._
import java.time.Instant

@RPC
trait ProjectApi {
  def listProject: Seq[Project]
  def getProject(id: String): Option[Project]
  def updateProject(project: Project, requestId: ULID = ULID.newULID): Option[Project]
  def createProject(project: Project, requestId: ULID = ULID.newULID): Option[Project]
  def deleteProject(projectId: String, requestId: ULID = ULID.newULID): Unit
}

object ProjectApi {
  case class Project(
      // The server-generated project ID
      id: String,
      // The name for referencing this project
      name: String,
      // The description of the project
      description: String,
      // The time when the project was created
      createdAt: Instant
  )
}
