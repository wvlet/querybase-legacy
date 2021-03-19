package wvlet.querybase.ui

import wvlet.airframe._
import wvlet.airframe.control.CircuitBreakerOpenException
import wvlet.airframe.http.js.JSHttpClient
import wvlet.airframe.rx.{Rx, RxOption, RxStream}
import wvlet.log.LogSupport
import wvlet.querybase.api.frontend.ServiceJSClient

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  */
trait RPCService extends LogSupport {
  protected implicit val queue = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private lazy val rpcClient               = bind[ServiceJSClient]
  protected val jsHttpClient: JSHttpClient = rpcClient.getClient

  def rpc[U](body: ServiceJSClient => Future[U]): Future[U] = {
    val future = body(rpcClient)
    future.onComplete {
      case Success(_) =>
      // Do nothing
      case Failure(c: CircuitBreakerOpenException) =>
        error(c.getMessage)
      case Failure(e) =>
        error(e)
    }
    future
  }

  def rpcRx[U](body: ServiceJSClient => Future[U]): RxStream[U] = {
    Rx.future(rpc(body))
  }

  def repeatRpc[U](time: Long, unit: TimeUnit)(body: ServiceJSClient => Future[U]): RxStream[U] = {
    Rx
      .interval(time, unit)
      .andThen(i => body(rpcClient))
  }

}
