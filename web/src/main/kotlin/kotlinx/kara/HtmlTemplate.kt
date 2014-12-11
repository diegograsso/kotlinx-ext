package kotlinx.kara

import javax.servlet.http.HttpServletResponse
import kotlinx.html.HTML

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
