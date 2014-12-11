package kotlinx.kara.tests

import kotlinx.kara.*
import kotlinx.html.h2
import kotlinx.html.p

/** Home html view.
 */
fun HomeView() = HtmlTemplateView<DefaultTemplate>(DefaultTemplate()) {
    content {
        h2 {
            +"Welcome Home"
        }
        p {
            +"<h2>MakeSureThisIsEscaped</h2>"
        }
        p { +"<h2>MakeSureThisIsEscaped</h2>" }
    }
}
