package wvlet.querybase.server.frontend.code

import wvlet.airspec.AirSpec
import wvlet.querybase.api.ServiceSyncClient
import wvlet.airframe.Design
import wvlet.querybase.server.frontend.FrontendServer.QuerybaseSyncClient
import wvlet.querybase.api.frontend.code.ProjectApi.Project
import wvlet.airframe.control.ULID
import wvlet.querybase.server.frontend.FrontendServer

class ProjectApiTest extends AirSpec {

  override protected def design: Design = FrontendServer.testDesign

  test("project api") { client: QuerybaseSyncClient =>
    client.ProjectApi.createProject(
      project = Project(
        name = "my_project",
        description = "An example project",
        path = ""
      ),
      requestId = ULID.newULID
    )
  }
}
