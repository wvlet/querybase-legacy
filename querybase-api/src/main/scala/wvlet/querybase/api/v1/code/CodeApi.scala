package wvlet.querybase.api.v1.code

import wvlet.airframe.http.RPC
import wvlet.airframe.control.ULID

/** Project: A container for managing list of files
  * Workbook: A work note for writing queries
  */
object CodeApi {


  case class Workbook(id: String, name: String, description: String)
  case class WorkbookDetail()
  case class CreateWorkbookRequest(name: String, description: String, ulid: ULID = ULID.newULID)
  case class UpdateWorkbookRequest(id: String, )

}

import CodeApi._

@RPC
trait CodeApi {
  def listProject: Seq[Project]
  def getProject(id: String): Option[ProjectDetail]
  def updateProject(updateProjectRequest: UpdateProjectRequest): Option[ProjectDetail]
  def createProject(createProjectRequest: CreateProjectRequest): Option[ProjectDetail]
  def deleteProject(deleteProjectRequest: DeleteProjectRequest): Unit

}
