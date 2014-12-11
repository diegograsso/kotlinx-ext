package kara.demo.views

import kara.demo.styles.DefaultStyles
import kara.demo.styles.StyleClasses.*
import kotlinx.kara.*
import kotlinx.html.*
import javax.swing.text.DefaultEditorKit.InsertBreakAction

class DetailsTemplate : Template<HTML>() {
    val caption = Placeholder<HtmlBodyTag>()
    val subject = Placeholder<HtmlBodyTag>()
    val message = Placeholder<HtmlBodyTag>()

    override fun HTML.render() {
        with (DefaultTemplate()) {
            content {
                h3 {
                    insert(caption)
                }
                p(title) {
                    insert(subject)
                }
                div(body) {
                    insert(message)
                }

            }
            render()
        }
    }

}
