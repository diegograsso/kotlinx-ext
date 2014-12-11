package kotlinx.kara.tests

import kotlinx.kara.Get
import kotlinx.kara.Request
import kotlinx.kara.Post
import kotlinx.kara.TextResult
import kotlinx.html.*
import kotlinx.kara.Put
import kotlinx.kara.Delete
import kotlinx.kara.ErrorResult

object Routes {
    Get("/")
    class Index() : Request({ HomeView() })

    Get("/test")
    class Test() : Request({ TextResult("This is a test action") })

    Post("/update")
    class Update() : Request({ TextResult("Something's been updated!") })

    Get("/optional/?p")
    class Optional(val p: String?) : Request({
        TextResult("optional/${p}")
    })

    Get("/template/:n")
    class SomeRoute(n: Int) : Request({
        when (n) {
            1 -> view {
                header {
                    div {

                    }
                }

                content {
                    left {
                        span {

                        }
                    }
                }
            }
            2 -> SomeFunView()
            else -> ErrorResult(404, "Not found")
        }
    })

    object Foo {
        Get("#")
        class Blank() : Request({
            TextResult("blank")
        })

        Get("bar")
        class Bar() : Request({
            TextResult("bar")
        })

        Get("bar/baz")
        class Barbaz() : Request({
            TextResult("bar/baz")
        })

        Get("#")
        class Foobar() : Request({
            TextResult("foobar")
        })

        Get("*/list")
        class List() : Request({
            TextResult("list: ${params[0]}")
        })

        Get("complex/*/list/:id")
        class Complex(id: String) : Request({
            TextResult("complex: ${params[0]} id = $id")
        })

        Get("redirect")
        class Redirect() : Request({
            redirect("/foo/bar")
        })

        Get("compute/:anInt/:aFloat")
        class Compute(val anInt: Int, val aFloat: Float) : Request({
            TextResult("compute: ${anInt}, ${aFloat}")
        })

        Get("compute")
        class ComputeQuery(val anInt: Int, val aFloat: Float) : Request({
            TextResult("compute: ${anInt}, ${aFloat}")
        })
    }

    object Crud {
        Get("")
        class Index() : Request({
            TextResult("index")
        })

        Get(":id")
        class Show(id: Int) : Request({
            TextResult("show $id")
        })

        Post("")
        class Create() : Request({
            TextResult("create")
        })

        Put(":id")
        class Update(id: Int) : Request({
            TextResult("update ${id}")
        })

        Delete(":id")
        class _Delete(id: String) : Request({
            TextResult("delete $id")
        })
    }
}
