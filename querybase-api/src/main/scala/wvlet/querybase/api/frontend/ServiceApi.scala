package wvlet.querybase.api.frontend

import wvlet.airframe.http.RPC
import wvlet.airframe.metrics.ElapsedTime
import wvlet.querybase.api.BuildInfo

import java.time.Instant

/**
  */
@RPC
trait ServiceApi {
  private val serviceStartTimeMillis = System.currentTimeMillis()

  import ServiceApi._
  def serviceInfo: ServiceInfo = {
    ServiceInfo(upTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - serviceStartTimeMillis))
  }

  def serviceNodes: Seq[ServiceNode]
}

object ServiceApi {
  case class ServiceInfo(
      name: String = "querybase",
      version: String = BuildInfo.version,
      oauthClientId: Option[String] = None,
      upTime: ElapsedTime
  )

  case class ServiceNode(name: String, address: String, startedAt: Instant, lastHeartBeatAt: Instant) {
    def upTime: ElapsedTime = ElapsedTime.succinctMillis(System.currentTimeMillis() - startedAt.toEpochMilli)
  }
}
