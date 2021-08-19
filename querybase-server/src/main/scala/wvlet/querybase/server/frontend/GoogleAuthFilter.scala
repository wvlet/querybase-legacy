package wvlet.querybase.server.frontend

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import wvlet.airframe.http.{Http, HttpStatus}
import wvlet.log.LogSupport

import scala.jdk.CollectionConverters._

case class GoogleAuthConfig(
    clientId: String = "793299428025-n6kmmrmcs4g80kibc7m7qakn6vc656bt.apps.googleusercontent.com"
)

/** */
class GoogleAuthFilter(config: GoogleAuthConfig) extends AuthFilter with LogSupport {

  private val verifier = {
    new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport, GsonFactory.getDefaultInstance())
      .setAudience(List(config.clientId).asJava)
      .build()
  }

  override def apply(request: Request, context: Context): Future[Response] = {
    request.authorization match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val authToken = authHeader.stripPrefix("Bearer ").trim
        Option(verifier.verify(authToken)) match {
          case Some(idToken) =>
            context.setThreadLocal("gauth", idToken)
            context(request)
          case None =>
            throw Http.serverException(HttpStatus.Unauthorized_401)
        }
      case _ =>
        throw Http.serverException(HttpStatus.Unauthorized_401)
    }

  }
}
