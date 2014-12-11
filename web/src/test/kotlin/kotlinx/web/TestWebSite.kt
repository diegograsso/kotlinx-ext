package kotlinx.web.testsite

import kotlinx.web.FileResourceHandler
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import kotlin.reflect.KMemberFunction0
import kotlin.reflect.KMemberFunction1
import kotlin.reflect.jvm.internal.KMemberFunctionImpl
import kotlinx.jdk.numbers.minimum

public class Server(val ioThreads: Int = Runtime.getRuntime().availableProcessors().minimum(2),
                    val workerThreads: Int = (ioThreads * 8).minimum(8))   {

}

public fun main(args: Array<String>) {
    /* ideas
   val server = ServerBuilder {
       ioThreads = 8
       workerThreads = 64
       listenOn {
           protocol = HTTP
           port = 8080
       }
       route("/", FileResourceHandler("/Users/jminard/DEV/Collokia/oss/reakt/todo")) {

       }

       location("/") {        // apply versioning at this level?
           staticRespources FileResourceHandler(configuration.rootFilePath, "index.html").usingVersioning(listOf(MIME_ALL_CSS, MIME_ALL_JAVASCRIPT, MIME_ALL_IMAGES, MIME_ALL_HTML).flatMap())
           caching {
               clientCacheControl {
                   mimeTypes = MIME_ALL_CSS
               }
           }
           location("css") {
               translate {
                   from = ".sass"
                   to   = ".css"
                   translator = SassResourceTranslator
               }
               interceptBefore pathsEndingWith ".css" withExistingFile ".sass" {  request, existingFile, nextHandler ->
                   return CacheControl(SassCompiledResponse(existingFile)).onServerFor(4.hours)).onClientFor(30.days)
               }
               interceptBefore pathsEndingWith ".css" withExistingFile ".less" {  request, existingFile, nextHandler ->
                   return CacheControl(LessCompiledResponse(existingFile)).onServerFor(4.hours)).onClientFor(30.days)
               }
               interceptAfter mimeTypesCSS { request, response ->  // todo: known CSS type list
                   return CacheControl(response).onClientFor(30.days)
               }
           }
           location("images") {
               interceptAfter mimeTypesImages { request, response -> // todo: known image type list
                   return CacheControl(response).onClientFor(30.days)
               }      // also pathsEndingWith
           }
           location("js") {
               interceptAfter mimeTypesJavaScript { request, response -> // todo: known js type list
                   return CacheControl(NonBlockingBinaryResponse(request)).onClientFor(30.days)
               }      // also pathsEndingWith
           }
           interceptAfter pathsEndingWith listOf(".html", ".xhtml") { request, response -> // not using mime type because other services return mime types
               return CacheControl(NonBlockingBinaryResponse(request)).onClientFor(30.days)
           }      // also pathsEndingWith
       }
   }.build()
//   server.start()

    TestThing("hello").dele()

    println("==== root ====")
    val root = Route.from("/").too(TestThing::printMyVal)
    println("==== root2 ====")
    val root2 = Route.from("/").too2(TestThing::printMyVal)

    println("==== root3 ====")
    val root3 = too3("/", TestThing::printMyVal)

    println("==== root3 without assign ====")
    too3("/", TestThing::printMyVal)

    println("done")
    */
}





inline fun <reified T> TestThing.foo(x: T) {
     println(" =0 ${x.javaClass.toString()}")
     println("  1 ${x.javaClass.getSuperclass()}")
     println("  2 ${x.javaClass.getGenericSuperclass()}")
     println("  3 ${x.javaClass.getInterfaces().joinToString(",")}")
     println("  4 ${x.javaClass.getGenericInterfaces().joinToString(",")}")
}


public class TestThing(val myValue: String) {
    public fun printMyVal(i: Int): Response {
        println("${myValue} ${i}")
        return Response()
    }

    val x = TestThing::printMyVal

    public fun dele() {
        with (TestThing("other instance")) { x(3) }
        x(1)

        println(x.javaClass.getName())
        println(x.javaClass.getSuperclass().getName())
        if (x is KMemberFunction0<*,*>) {
            println("x is 0 param")
        }
        else if (x is KMemberFunction1<*,*,*>) {
            println("x is 1 param")
        }

        for (one in TestThing::printMyVal.javaClass.getAnnotations()) {
            println("  - ${one.toString()}")
        }

        foo(x)

    }
}

public class Route <OT, P1T, FT : KMemberFunction1<OT, P1T, Response>>(val path: String, val handler: FT, val ftClass: Class<FT>) {
    {
       // println("  >> handler in Route classname ${ftClass.getName()}")
    }
    class object {
        public fun from(path: String): RouteHandler {
            return RouteHandler(path)
        }
    }

}

public class RouteHandler(public val path: String) {
    public inline fun <reified OT, reified P1T, reified FT : KMemberFunction1<OT, P1T, Response>> too(handler: FT): Route<OT, P1T, FT> {
        println("  >> handler classname ${handler.javaClass.getName()}")
        println("  >> handler ancestor ${handler.javaClass.getSuperclass()}")
        println("  >> handler generic ancestor ${handler.javaClass.getGenericSuperclass()}")
        println("  >> handler interfaces ${handler.javaClass.getInterfaces().joinToString(",")}")
        println("  >> handler generic interfaces ${handler.javaClass.getGenericInterfaces().joinToString(",")}")
        println("  >> P1T classname ${javaClass<P1T>().getName()}")
        println("  >> OT classname ${javaClass<OT>().getName()}")
        println("  >> FT classname ${javaClass<FT>().getName()}")
        return Route(path, handler, handler.javaClass)
    }

    public fun <OT, P1T, FT : KMemberFunction1<OT, P1T, Response>> too2(handler: FT): Route<OT, P1T, FT> {
        println("  >> handler classname ${handler.javaClass.getName()}")
        println("  >> handler ancestor ${handler.javaClass.getSuperclass()}")
        println("  >> handler generic ancestor ${handler.javaClass.getGenericSuperclass()}")
        println("  >> handler interfaces ${handler.javaClass.getInterfaces().joinToString(",")}")
        println("  >> handler generic interfaces ${handler.javaClass.getGenericInterfaces().joinToString(",")}")
        return Route(path, handler, handler.javaClass)
    }
}

public inline fun <reified OT, reified P1T, reified FT : KMemberFunction1<OT, P1T, Response>> too3(path: String, handler: FT): Route<OT, P1T, FT> {
    println("  >> handler classname ${handler.javaClass.getName()}")
    println("  >> handler ancestor ${handler.javaClass.getSuperclass()}")
    println("  >> handler generic ancestor ${handler.javaClass.getGenericSuperclass()}")
    println("  >> handler interfaces ${handler.javaClass.getInterfaces().joinToString(",")}")
    println("  >> handler generic interfaces ${handler.javaClass.getGenericInterfaces().joinToString(",")}")
    println("  >> P1T classname ${javaClass<P1T>().getName()}")
    println("  >> OT classname ${javaClass<OT>().getName()}")
    println("  >> FT classname ${javaClass<FT>().getName()}")

    println("  drill into interface...")
    for (one in handler.javaClass.getGenericInterfaces()) {
            if (one is java.lang.reflect.ParameterizedType) {
                println("    Found ParameterizedType of ${one.javaClass.getName()}")
                println("      Annotations:")
                for (anno in TestThing::printMyVal.javaClass.getAnnotations()) {
                    println("       Class Annotation ${anno.toString()}")
                }
                println("      Types: owner=${one.getOwnerType()}, raw=${one.getRawType()}, parms=${one.getActualTypeArguments().joinToString(",")}")

            }

    }
    return Route(path, handler, handler.javaClass)
}

public class Response() {

}

