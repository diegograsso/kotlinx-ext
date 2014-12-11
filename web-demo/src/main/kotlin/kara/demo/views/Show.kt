package kara.demo.views

import kara.demo.models.*
import kara.demo.styles.StyleClasses.*
import kara.demo.styles.StyleClasses

import kotlinx.html.*
import kotlinx.kara.HtmlTemplateView

fun Show(post: Post) = HtmlTemplateView(DetailsTemplate()) {
    caption {
        h3 { +"Show Post" }
        div{
            setClass(date)
            label { +"Date:" }
            +"${post.date}"
        }
    }

    subject {
        +"Title: ${post.title}"
    }
    message {
        +post.body
    }

}
