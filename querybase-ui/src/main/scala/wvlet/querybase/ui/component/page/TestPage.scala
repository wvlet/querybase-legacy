package wvlet.querybase.ui.component.page

import wvlet.airframe.bind
import wvlet.airframe.metrics.ElapsedTime
import wvlet.airframe.rx.{Rx, RxOption}
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.log.LogSupport
import wvlet.querybase.ui.test.{RPCTest, RxTest}

class TestPage extends RxElement with LogSupport {

  private val testSuites: Seq[RxTest] = Seq(
    bind[RPCTest]
  )

  override def render: RxElement = div(
    table(
      cls -> "table",
      thead(
        tr(
          Seq("suite", "test name", "elapsed", "check").map { x =>
            th(x)
          }
        )
      ),
      tbody(
        testSuites.map { suite =>
          val startNanos = System.nanoTime()
          val finished   = Rx.variable[Boolean](false)
          info(s"[${suite.name}]")
          suite.tests.reverse.map { t =>
            info(s"- ${t.testName}")
            tr(
              td(t.suite),
              td(t.testName),
              td(
                finished.map {
                  case false => "---"
                  case true  => ElapsedTime.nanosSince(startNanos).toString()
                }
              ),
              td(
                t.body() match {
                  case r: RxOption[_] =>
                    r.transform {
                      case None =>
                        span(cls -> s"badge badge-success", "RUNNING")
                      case Some(x) => {
                        finished := true
                        span(cls -> s"badge badge-info", "FINISHED")
                      }
                    }.recover { case e: Throwable =>
                        finished := true
                        span(cls -> s"badge badge-danger", "FAILED")
                      }
                  case _ =>
                    span()
                }
              )
            )
          }
        }
      )
    )
  )
}
