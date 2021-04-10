package wvlet.querybase.server.frontend

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import wvlet.airframe.http.finagle.FinagleFilter

/**
  */
trait AuthFilter extends FinagleFilter

object NoAuthFilter extends AuthFilter {
  override def apply(request: Request, context: NoAuthFilter.Context): Future[Response] = {
    context(request)
  }
}
