package kotlinx.kara

import javax.servlet.http.HttpServletResponse
import kotlinx.html.HtmlBodyTag
import kotlinx.html.HTML

/** Base class for html views.
 */
abstract class HtmlView(val layout: HtmlLayout? = null) : ActionResult {
    override fun writeResponse(context: ActionContext): Unit {
        writeResponse(context.response)
    }

    fun writeResponse(response: HttpServletResponse) {
        response.setContentType("text/html")
        val writer = response.getWriter()!!
        if (layout == null) {
            writer.write(renderWithoutLayout())
        }
        else {
            val page = HTML()
            with(layout) {
                page.render(this@HtmlView)
            }
            writer.write(page.toString())
        }
        writer.flush()
    }

    fun renderWithoutLayout(): String {
        val root = object: HtmlBodyTag(null, "view") {
        }
        root.render()

        val builder = StringBuilder()
        for (child in root.children) {
            child.renderElement(builder, "")
        }
        return builder.toString()
    }


    /** Subclasses must implement this to provide the primary html to dispay.
     */
    abstract fun HtmlBodyTag.render()
}

public fun HtmlBodyTag.renderView(view: HtmlView) : Unit = with(view) { render() }

