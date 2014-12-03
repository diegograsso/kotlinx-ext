package kotlinx.web

import io.undertow.util.PathMatcher

public class Server(routes: Route.()->Unit) {

}

public class Route {
    fun route(x: PathMatcher.PathMatch)
}

public fun main(args: Array<String>) {
    val server = Server {

    }
}