package kotlinx.kara

import javax.servlet.http.HttpServletResponse
import kotlinx.html.HTML
import kotlinx.html.DIV
import kotlinx.html.StyleClass

public open class HtmlTemplateView<T : Template<HTML>>(val template: T, val build: T.() -> Unit) : ActionResult {
    override fun writeResponse(context: ActionContext) {
        writeResponse(context.response)
    }

    fun writeResponse(response: HttpServletResponse) {
        response.setContentType("text/html")
        val writer = response.getWriter()!!
        val view = this@HtmlTemplateView
        val page = HTML()
        with(template) {
            with(view) { build() }
            with(page) { render() }
        }
        writer.write(page.toString())
        writer.flush()
    }
}

public open class DivTemplateView<T : Template<DIV>>(val template: T, val divId: String? = null, vararg divStyles: StyleClass, val build: T.() -> Unit) : ActionResult {
    val styles = divStyles
    override fun writeResponse(context: ActionContext) {
        writeResponse(context.response)
    }

    fun writeResponse(response: HttpServletResponse) {
        response.setContentType("text/html")
        val writer = response.getWriter()!!
        val view = this@DivTemplateView
        val page = DIV()
        with(template) {
            with(view) {
                build()
            }
            with(page) {
                if (view.divId != null) id = view.divId
                for (style in view.styles) addClass(style)
                render()
            }
        }
        writer.write(page.toString())
        writer.flush()
    }
}

public open class DivView(val divId: String? = null, vararg divStyles: StyleClass, val content: DIV.() -> Unit) : ActionResult {
    val styles = divStyles
    override fun writeResponse(context: ActionContext) {
        writeResponse(context.response)
    }

    fun writeResponse(response: HttpServletResponse) {
        response.setContentType("text/html")
        val writer = response.getWriter()!!
        val div = DIV()

        with(div) {
            if (divId != null) id = divId
            for (style in styles) addClass(style)
            content()
        }
        writer.write(div.toString())
        writer.flush()
    }
}