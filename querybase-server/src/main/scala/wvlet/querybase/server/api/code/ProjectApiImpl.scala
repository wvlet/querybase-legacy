package wvlet.querybase.server.api.code

import wvlet.querybase.api.v1.code.ProjectApi
import wvlet.airframe.control.ULID
import wvlet.log.LogSupport

trait ProjectApiImpl extends ProjectApi with LogSupport {

  def createProject(project: ProjectApi.Project, requestId: ULID): Option[ProjectApi.Project] = {
    info(s"Create Project: ${project}")
    None
  }
  def deleteProject(projectId: String, requestId: ULID): Unit = {}
  def getProject(id: String): Option[ProjectApi.Project] = {
    None
  }
  def listProject: Seq[ProjectApi.Project] = {
    Seq.empty
  }

  def updateProject(project: ProjectApi.Project, requestId: ULID): Option[ProjectApi.Project] = {
    None
  }
}
