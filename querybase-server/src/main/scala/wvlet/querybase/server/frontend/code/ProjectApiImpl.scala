package wvlet.querybase.server.frontend.code

import wvlet.airframe.bind
import wvlet.querybase.api.frontend.code.ProjectApi
import wvlet.airframe.ulid.ULID
import wvlet.log.LogSupport
import wvlet.querybase.server.backend.BackendServer.CoordinatorClient

trait ProjectApiImpl extends ProjectApi with LogSupport {
  import ProjectApi._

  private val coordinatorClient = bind[CoordinatorClient]

  def createProject(project: ProjectApi.Project, requestId: ULID): Option[ProjectApi.Project] = {
    info(s"Create Project: ${project}")
    None
  }

  def deleteProject(projectId: String, requestId: ULID): Unit = {}

  def getProject(id: String): Option[ProjectApi.Project] = {
    None
  }

  def listProject: Seq[ProjectApi.Project] = {
    Seq(
      Project(id = "1", name = "project 1", description = "project", path = "")
    )
  }

  def updateProject(project: ProjectApi.Project, requestId: ULID): Option[ProjectApi.Project] = {
    None
  }
}
