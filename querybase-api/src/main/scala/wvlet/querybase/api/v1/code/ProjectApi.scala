package wvlet.querybase.api.v1.code

import java.time.Instant
import wvlet.airframe.http.RPC
import wvlet.airframe.control.ULID

object ProjectApi {
  case class Project(
      // The server-generated project ID
      id: String = "N/A",
      // The name for referencing this project
      name: String,
      // The description of the project
      description: String,
      // File path
      path: String,
      // The time when the project was created
      createdAt: Instant = Instant.now()
  )

  case class Module(
      // The server-generated ID
      id: String = "N/A",
      // The module name
      name: String,
      // The description of the module
      description: String,
      // The time when the module was created
      createdAt: Instant = Instant.now()
  )

}

@RPC
trait ProjectApi {
  import ProjectApi._

  def listProject: Seq[Project]
  def getProject(id: String): Option[Project]
  def updateProject(project: Project, requestId: ULID): Option[Project]
  def createProject(project: Project, requestId: ULID): Option[Project]
  def deleteProject(projectId: String, requestId: ULID): Unit

}
