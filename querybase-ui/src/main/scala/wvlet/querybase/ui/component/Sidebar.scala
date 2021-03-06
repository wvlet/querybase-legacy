package wvlet.querybase.ui.component

/**
  */
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}
import wvlet.airframe.rx.html.{RxComponent, RxElement}
import wvlet.airframe.rx.html.all.{id, _}

/**
  */
class Sidebar(router: RxRouter) extends RxElement {

  private def layout: RxElement =
    div(
      cls   -> "flex-column flex-grow-0 flex-shrink-0 bg-light",
      style -> "width: 240px; ",
      nav(
        cls   -> "navbar navbar-dark bg-primary",
        style -> "height: 60px;",
        a(
          cls -> "navbar-brand",
          //img(src -> "img/favicon.ico", width -> 30, height -> 30, cls -> "d-inline-block align-top"),
          span(
            cls -> "px-1",
            "Querybase"
          ),
          onclick -> { e: MouseEvent =>
            router.setPath("/home")
            e.preventDefault()
          }
        )
      )
    )

  private def navItem(name: String, iconCls: String, path: String) = {
    li(
      cls -> "nav-item pl-3",
      onmouseover -> { e: MouseEvent =>
        e.currentTarget match {
          case el: HTMLElement =>
            el.style.backgroundColor = "#ddd0ff"
          case _ =>
        }
      },
      onmouseout -> { e: MouseEvent =>
        e.currentTarget match {
          case el: HTMLElement =>
            el.style.backgroundColor = ""
          case _ =>
        }
      },
      onclick -> { e: MouseEvent =>
        router.setPath(path)
      },
      a(
        cls -> "nav-link text-primary", // active",
        i(
          cls   -> s"fa ${iconCls} mr-2",
          style -> "width: 20px; "
        ),
        name
      )
    )
  }

  private def navFrame(items: RxElement*) = nav(
    cls -> "navbar px-0",
    ul(
      cls -> "navbar-nav flex-column my-0 w-100",
      items
    )
  )

  override def render: RxElement = {
    layout(
      div(
        cls   -> "flex-grow-1",
        style -> "height: calc(100vh - 60px);",
        navFrame(
          navItem("Home", "fa-home", "/home"),
          navItem("Explore", "fas fa-book-open", "/explore"),
          navItem("Services", "fa-project-diagram", "/services"),
          navItem("System", "fa-server", "/system")
        ),
        new FoldableMenu(elementId = "projectList", name = "Projects"),
        SidebarBorder,
        navFrame(
          navItem("Settings", "fa-cog", "/settings"),
          navItem("Help", "fa-question-circle", "/help"),
          navItem("Test", "fa-diagnoses", "/test")
        )
      )
    )
  }
}

object SidebarBorder extends RxElement {
  override def render = hr(cls -> "my-1")
}

class FoldableMenu(elementId: String, name: String) extends RxElement {

  override def render: RxElement = {
    div(
      cls -> "pl-2",
      SidebarBorder,
      span(
        cls  -> "btn text-secondary w-100 text-left",
        href -> s"#${elementId}",
        //role           -> "button",
        data("toggle") -> "collapse",
        data("target") -> s"#${elementId}",
        aria.expanded  -> false,
        aria.controls  -> elementId,
        i(
          cls -> "fas fa-caret-right mr-2"
        ),
        name
      ),
      div(
        cls -> "collapse show",
        id  -> s"${elementId}",
        div(
          cls -> "dropdown",
          div(
            cls  -> "dropdown-item",
            href -> "#",
            "Project 1"
          ),
          div(
            cls  -> "dropdown-item",
            href -> "#",
            "Project 2"
          )
        )
      )
    )
  }
}
