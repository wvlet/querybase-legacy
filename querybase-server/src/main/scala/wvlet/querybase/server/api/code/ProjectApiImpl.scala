package wvlet.querybase.server.api.code

import wvlet.querybase.api.v1.code.ProjectApi
import wvlet.airframe.control.ULID
import wvlet.log.LogSupport

trait ProjectApiImpl extends ProjectApi with LogSupport {

  import ProjectApi._

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
      Project(id = "1", name = "project 1", description = "project"),
      Project(id = "1", name = "project 2", description = "project")
    )
  }

  def updateProject(project: ProjectApi.Project, requestId: ULID): Option[ProjectApi.Project] = {
    None
  }
}
