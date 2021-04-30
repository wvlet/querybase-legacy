package wvlet.querybase.ui.component.explore

import org.scalajs.dom.raw.MouseEvent
import wvlet.airframe.rx.Rx
import wvlet.airframe.rx.html.RxElement
import wvlet.airframe.rx.html.all._
import wvlet.airframe.ulid.ULID
import wvlet.querybase.ui.component.common.VStack
import wvlet.querybase.ui.component._

import java.time.Instant

/**
  */
class TimelineView extends RxElement {
  override def render: RxElement = div(
    VStack(
      TimelineItemCard(TimelineItem(ULID.newULID, "query", "My query", "select 1")),
      TimelineItemCard(TimelineItem(ULID.newULID, "query", "My query", "select 10"))
    )
  )
}

case class TimelineItem(
    id: ULID,
    kind: String,
    title: String,
    summary: String
) {
  def createdAt: Instant = id.toInstant
}

case class TimelineItemCard(item: TimelineItem) extends RxElement {

  private val cardStyle = Rx.variable("card")

  override def render: RxElement = div(
    cardStyle.map { cls -> _ },
    style -> "width: 300px",
    onmouseover -> { e: MouseEvent =>
      cardStyle := s"card bg-light"
    },
    onmouseout -> { e: MouseEvent =>
      cardStyle := s"card"
    },
    div(
      cls -> "card-body",
      h6(
        cls -> "card-title",
        item.title
      ),
      span(
        cls -> "card-text text-info",
        item.summary
      ),
      div(
        cls -> "text-secondary",
        item.createdAt.toString
      )
    )
  )
}
