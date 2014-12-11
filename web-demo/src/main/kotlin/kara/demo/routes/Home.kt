package kara.demo.routes

import kotlinx.kara.*
import java.util.Date
import kara.demo.models.Book

object Home {
    Get("/")
    class Index() : Request({ kara.demo.views.Index() })

    Get("/test")
    class Test() : Request({
        TextResult("This is a test action, yo")
    })

    Get("/crash")
    class Crash() : Request({
        error("This resource crashes")
    })

    Post("/updatebook")
    class Update() : Request({
        println("parameters:")
        println(params.getHash("book").toString())
        redirect("/forms")
    })

    Get("/forms")
    class Forms() : Request({
        val book = Book("Ender's Game") {
            description = "This is an excellent book about a boy who gets drawn into an interstellar war."
            isPublished = true
        }
        kara.demo.views.Forms(book)
    })

    Get("/json")
    class JsonPage : Request({
        json {
            jsonObject {
                jsonValue("version", 5)
                jsonObject("people")
                {
                    jsonValue("id", 1)
                    jsonValue("name", "Ilya")
                }
                jsonArray("cities")
                {
                    jsonValue("a")
                    jsonValue("b")
                    jsonValue("c")
                    jsonObject {
                        jsonValue("x", "y")
                    }
                }
            }
        }
    })

    Get("/resources/less.css") object LessResource: EmbeddedLessResource("less/root.less")

    object Posts {
        Get("get/:id")
        class GetPost(id: Int) : Request({
            val post = kara.demo.models.Post(Date(), "Post ${id}")
            post.body = "This is the <em>post</em> body"
            kara.demo.views.Show(post)
        })
    }
}
