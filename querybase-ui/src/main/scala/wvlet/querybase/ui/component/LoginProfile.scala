package wvlet.querybase.ui.component

import org.scalajs.dom
import wvlet.airframe.rx.{Rx, RxOptionVar, RxVar}
import wvlet.airframe.rx.html.all.s
import wvlet.log.LogSupport

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.timers

/**
  *
  */
case class LoginProfile(
    name: String,
    email: String,
    imageUrl: String,
    id_token: String
)

object LoginProfile extends LogSupport {
  val currentUser: RxOptionVar[LoginProfile] = Rx.optionVariable(None)

  /**
    * Initialize GoogleAPI Auth2, and return a Future, which will be set to true
    * after the initialization completed.
    */
  private[ui] def init: Future[Boolean] = {
    val isInitialized = Promise[Boolean]()
    js.Dynamic.global.gapi.load(
      "auth2",
      () => {
        val auth2 = js.Dynamic.global.gapi.auth2
          .init(
            js.Dynamic
              .literal(
                client_id = s"793299428025-n6kmmrmcs4g80kibc7m7qakn6vc656bt.apps.googleusercontent.com",
                fetch_basic_profile = true
              )
          )

        auth2.isSignedIn.listen((isSignedIn: Boolean) => {
          debug(s"isSignedIn: ${isSignedIn}")
          if (isSignedIn) {
            updateUser
          } else {
            LoginProfile.currentUser := None
          }
        })

        auth2.`then`({ () =>
          debug(s"gapi.auth2 is initialized")
          isInitialized.success(true)
        })
      }
    )

    // Refresh auth token for every 45 minutes
    timers.setInterval(45 * 60 * 1000) {
      refreshAuth
    }

    isInitialized.future
  }

  private[ui] def signOut: Unit = {
    val auth2 = js.Dynamic.global.gapi.auth2.getAuthInstance()
    auth2.signOut()
    LoginProfile.currentUser := None
    debug(s"Signed out")
    dom.document.location.reload()
  }

  private def refreshAuth: Unit = {
    debug(s"Refreshing oauth2 token")
    val user = js.Dynamic.global.gapi.auth2.getAuthInstance().currentUser.get()
    user.reloadAuthResponse().`then` { () =>
      updateUser
    }
  }

  private def updateUser: Unit = {
    val googleUser = js.Dynamic.global.gapi.auth2.getAuthInstance().currentUser.get()
    val token      = googleUser.getAuthResponse().id_token
    val profile    = googleUser.getBasicProfile()
    LoginProfile.currentUser := Some(
      LoginProfile(
        name = s"${profile.getName()}",
        email = s"${profile.getEmail()}",
        imageUrl = s"${profile.getImageUrl()}",
        id_token = token.toString
      )
    )
  }
}
