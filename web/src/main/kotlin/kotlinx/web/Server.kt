package kotlinx.web

import io.undertow.util.PathMatcher

public class Server(init: Server.() -> Unit) {
    {
        with (this) { init() }
    }


    inner class Route {

    }
}



public fun main(args: Array<String>) {
    val server = Server {

    }
}