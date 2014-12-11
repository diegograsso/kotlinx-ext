package kotlinx.web

import io.undertow.util.PathMatcher
import java.util.LinkedList
import kotlin.properties.Delegates
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.LinkedHashSet
import io.undertow.Undertow
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import kotlinx.jdk.numbers.minimum
import io.undertow.server.handlers.accesslog.AccessLogHandler
import io.undertow.server.handlers.accesslog.AccessLogReceiver
import org.slf4j.LoggerFactory
import io.undertow.UndertowOptions
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.server.handlers.resource.FileResourceManager
import java.io.File
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

public enum class ServerProtocol {
    HTTP
    HTTPS
    WEBSOCKETS
}

public class ServerBuilder(init: ServerBuilder.() -> Unit = {}) {
    private val _allListeners = arrayListOf<Listener>()
    public val listeners: List<Listener>
        get() { return _allListeners }

    private val _allRoutes = arrayListOf<Route>()
    public val routes: List<Route>
        get() { return _allRoutes }

    public var accessLogFormat: String = "%t %a %p \"%r\" %q %s %b %Dms"
    public var ioThreads: Int = Runtime.getRuntime().availableProcessors()
    public var workerThreads: Int = Runtime.getRuntime().availableProcessors() * 8

    ; {
        with (this) { init() }
    }

    public fun build(): Server {
         return Server(_allListeners, ioThreads, workerThreads, accessLogFormat)
    }

    private fun checkDefaultListener(): Unit {
        if (_allListeners.isEmpty()) {
            _allListeners.add(Listener())
        }
    }

    public fun ServerBuilder.listenOn(init: Listener.() -> Unit): Unit {
        val newListener = Listener()
        this@ServerBuilder._allListeners.add(newListener)
        newListener.init()
    }

    public fun <RT: HttpHandler> ServerBuilder.route(pathTemplate: String, handler: RT, init: RT.() -> Unit = {}): Unit {
        val newRoute = Route(pathTemplate, handler)
        this@ServerBuilder._allRoutes.add(newRoute)
        with (handler) { init() }
    }



}

class Listener(var host: String = "0.0.0.0", var port: Int = 8080, var protocol: ServerProtocol = ServerProtocol.HTTP) {
    public val HTTP: ServerProtocol = ServerProtocol.HTTP
    public val HTTPS: ServerProtocol = ServerProtocol.HTTPS
    public val WEBSOCKETS: ServerProtocol = ServerProtocol.WEBSOCKETS
}

class Route(val pathTemplate: String, val handler: HttpHandler)  { }


open public class FileResourceHandler(path: String, transferMinSize: Long = 1024, caseSensitive: Boolean = true, followSymLinks: Boolean = true, followSafePaths: List<String> = listOf(path), welcomeFiles: List<String> = listOf("index.html")): ResourceHandler(FileResourceManager(File(path), transferMinSize, caseSensitive, followSymLinks, *followSafePaths.copyToArray())) {
    {
        addWelcomeFiles(*welcomeFiles.copyToArray())
    }
}

open public class Resource() {

}

open public class ErrorResult(val httpCode: Int, val msg: String): Resource() {

}

open public class NullHttpHandler() : HttpHandler {
    override fun handleRequest(exchange: HttpServerExchange?) {
        throw UnsupportedOperationException("no handler was defined") // TODO: message with route info
    }

}

public class Server(val listeners: List<Listener>,
                    requestedIoThreads: Int,
                    requestedWorkerThreads: Int,
                    val accessLogFormat: String) {
    val accessLogger = LoggerFactory.getLogger("http.access")!!

    val undertow = run {
        val ioThreads = (if (requestedIoThreads <= 0) Runtime.getRuntime().availableProcessors() else requestedIoThreads).minimum(2)
        val workerThreads = (if (requestedWorkerThreads <= 0) Runtime.getRuntime().availableProcessors() * 8 else requestedWorkerThreads).minimum(8)

        val rootHandler = ResourceHandler(FileResourceManager(File("/Users/jminard/DEV/Collokia/oss/reakt/todo"), 1024)).addWelcomeFiles("index.html")

        val outerHandler = AccessLogHandler(rootHandler, object : AccessLogReceiver {
            override fun logMessage(message: String?) {
                accessLogger.info(message)
            }
        }, accessLogFormat, javaClass<Server>().getClassLoader())

        val builder = Undertow.builder()
        .setDirectBuffers(true)
        .setIoThreads(ioThreads)
        .setWorkerThreads(workerThreads)
        .setHandler(outerHandler)
        .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)  // TODO: allow configuration?
        listeners.forEach { listener ->
            if (listener.protocol == ServerProtocol.HTTP) {
                builder.addHttpListener(listener.port, listener.host)
            }
            else if (listener.protocol == ServerProtocol.HTTPS) {
                throw IllegalArgumentException("HTTPS not yet supported")
            }
            else if (listener.protocol == ServerProtocol.WEBSOCKETS) {
                throw IllegalArgumentException("WebSockets not yet supported")
            }
            else {
                throw IllegalArgumentException("Unknown protocol: ${listener.protocol}")
            }
        }
        builder.build()
    }

    public fun start() {
        undertow.start()
    }

    public fun stop() {
        undertow.stop()
    }

}


