package wvlet.querybase.ui

import org.scalajs.dom.ext.AjaxException
import wvlet.airframe._
import wvlet.airframe.http.HttpStatus
import wvlet.airframe.http.js.JSHttpClient
import wvlet.log.LogSupport
import wvlet.querybase.api.ServiceJSClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  */
trait RPCService extends LogSupport {
  implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private lazy val rpc           = bind[ServiceJSClient]
  val jsHttpClient: JSHttpClient = rpc.getClient

  def rpc[U](body: ServiceJSClient => Future[U]): Future[U] = {
    val future = body(rpc)
    future.onComplete {
      case Success(_) =>
      // Do nothing
      case Failure(e: AjaxException) =>
        if (e.xhr.status == HttpStatus.Unauthorized_401.code) {
          // Handle authentication failure
          // TODO
          org.scalajs.dom.window.location.replace("/")
        }
      case Failure(e) =>
        error(e)
    }
    future
  }

}
