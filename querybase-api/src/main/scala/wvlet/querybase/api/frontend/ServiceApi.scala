package wvlet.querybase.api.frontend

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime
import wvlet.querybase.api.BuildInfo

/**
  */
@RPC
trait ServiceApi {
  private val serviceStartTimeMillis = System.currentTimeMillis()

  import ServiceApi._
  def serviceInfo: ServiceInfo = {
    ServiceInfo(upTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - serviceStartTimeMillis))
  }
}

object ServiceApi {
  case class ServiceInfo(
      name: String = "querybase",
      version: String = BuildInfo.version,
      oauthClientId: Option[String] = None,
      upTime: ElapsedTime
  )
}