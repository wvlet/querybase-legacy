package wvlet.querybase.server.api.code

import wvlet.airspec.AirSpec
import wvlet.querybase.api.ServiceSyncClient
import wvlet.airframe.Design
import wvlet.querybase.server.QuerybaseServer
import wvlet.querybase.server.QuerybaseServer.QuerybaseSyncClient
import wvlet.querybase.api.v1.code.ProjectApi.Project
import wvlet.airframe.control.ULID
import java.{util => ju}

class ProjectApiTest extends AirSpec {

  override protected def design: Design = QuerybaseServer.testDesign

  test("project api") { client: QuerybaseSyncClient =>
    client.ProjectApi.createProject(
      project = Project(
        name = "my_project",
        description = "An example project"
      ),
      requestId = ju.UUID.randomUUID()
    )
  }
}
