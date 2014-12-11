package kotlinx.kara.tests

import kotlinx.kara.Template
import kotlinx.kara.Placeholder
import kotlinx.html.*
import kotlinx.kara.insert

class DefaultTemplate : Template<HTML>() {
    val content = Placeholder<BODY>()

    override fun HTML.render() {
        head {
            title {+"This is the default layout"}
        }
        body {
            h1 {+"Default Layout"}
            insert(content)
        }
    }
}


