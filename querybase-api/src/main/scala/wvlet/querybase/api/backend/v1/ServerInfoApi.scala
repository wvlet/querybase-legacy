package wvlet.querybase.api.backend.v1

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime

/** */
@RPC
trait ServerInfoApi {
  private val serviceStartTimeMillis = System.currentTimeMillis()

  import ServerInfoApi._
  def serverInfo: ServerInfo = {
    ServerInfo(upTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - serviceStartTimeMillis))
  }
}

object ServerInfoApi {
  case class ServerInfo(
      name: String = "querybase",
      version: String = wvlet.querybase.api.BuildInfo.version,
      upTime: ElapsedTime
  )
}
