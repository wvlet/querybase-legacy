package wvlet.querybase.ui

import wvlet.airframe._
import wvlet.airframe.http.js.JSHttpClient
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.ServiceJSClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  */
trait RPCService extends LogSupport {
  protected implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private lazy val rpcClient               = bind[ServiceJSClient]
  protected val jsHttpClient: JSHttpClient = rpcClient.getClient

  protected def rpc[U](body: ServiceJSClient => Future[U]): Future[U] = {
    val future = body(rpcClient)
    future.onComplete {
      case Success(_) =>
      // Do nothing
      case Failure(e) =>
        error(e)
    }
    future
  }

}
