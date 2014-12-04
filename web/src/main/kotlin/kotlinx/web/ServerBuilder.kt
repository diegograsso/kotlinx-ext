package kotlinx.web

import io.undertow.util.PathMatcher
import java.util.LinkedList
import kotlin.properties.Delegates
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.LinkedHashSet

public enum class ServerProtocol {
    HTTP
    HTTPS
    WEBSOCKETS
}

public class ServerBuilder(init: ServerBuilder.() -> Unit = {}) {
    private val _allListeners = arrayListOf<Listener>()
    public val allListeners: List<Listener>
        get() { return _allListeners }

    ; {
        with (this) { init() }
    }

    public fun build(): Server {
         return Server()
    }


    private fun checkDefaultListener(): Unit {
        if (_allListeners.isEmpty()) {
            _allListeners.add(Listener())
        }
    }

    public fun ServerBuilder.listen(init: Listener.() -> Unit): Unit {
        val newListener = Listener()
        this@ServerBuilder._allListeners.add(newListener)
        newListener.init()
    }

    open inner class Listener(var host: String = "0.0.0.0", var port: Int = 8080, var protocol: ServerProtocol = ServerProtocol.HTTP) {
        public val HTTP: ServerProtocol = ServerProtocol.HTTP
        public val HTTPS: ServerProtocol = ServerProtocol.HTTPS
        public val WEBSOCKETS: ServerProtocol = ServerProtocol.WEBSOCKETS

        public fun port(port: Int): Listener {
            this.port = port
            return this
        }

        public fun protocol(proto: ServerProtocol): Listener {
            this.protocol = proto
            return this
        }
    }
}

public class Server() {

}


