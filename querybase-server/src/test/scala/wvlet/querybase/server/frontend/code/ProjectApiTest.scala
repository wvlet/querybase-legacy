package wvlet.querybase.server.frontend.code

import wvlet.airframe.Design
import wvlet.airframe.ulid.ULID
import wvlet.airspec.AirSpec
import wvlet.querybase.api.frontend.code.ProjectApi.Project
import wvlet.querybase.server.frontend.FrontendServer
import wvlet.querybase.server.frontend.FrontendServer.FrontendClient

class ProjectApiTest extends AirSpec {

  override protected def design: Design = FrontendServer.testDesign

  test("project api") { client: FrontendClient =>
    client.code.ProjectApi.createProject(
      project = Project(
        name = "my_project",
        description = "An example project",
        path = ""
      ),
      requestId = ULID.newULID
    )
  }
}
