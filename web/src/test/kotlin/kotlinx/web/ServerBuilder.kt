package kotlinx.web

import kotlinx.flow.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.server.handlers.resource.FileResourceManager
import java.io.File


public class ServerBuilderTests {
    Test fun testListeners() {
        isolatedTest {
            val server = ServerBuilder {
                listenOn {
                    port = 8443
                    host = "127.0.0.1"
                    protocol = HTTPS
                }
            }

            assertEquals(1, server.listeners.size())
            assertEquals("127.0.0.1", server.listeners[0].host)
            assertEquals(8443, server.listeners[0].port)
            assertEquals(ServerProtocol.HTTPS, server.listeners[0].protocol)
        }

        isolatedTest {
            val server = ServerBuilder {
                listenOn {
                    port = 9080
                    host = "0.0.0.0"
                }
                listenOn {
                    port = 9443
                    host = "127.0.0.1"
                    protocol = HTTPS
                }
                listenOn {
                    port = 9092
                    host = "127.0.0.1"
                    protocol = WEBSOCKETS
                }
            }

            assertEquals(3, server.listeners.size())

            assertEquals("0.0.0.0", server.listeners[0].host)
            assertEquals(9080, server.listeners[0].port)
            assertEquals(ServerProtocol.HTTP, server.listeners[0].protocol)
            assertEquals("127.0.0.1", server.listeners[1].host)
            assertEquals(9443, server.listeners[1].port)
            assertEquals(ServerProtocol.HTTPS, server.listeners[1].protocol)
            assertEquals("127.0.0.1", server.listeners[2].host)
            assertEquals(9092, server.listeners[2].port)
            assertEquals(ServerProtocol.WEBSOCKETS, server.listeners[2].protocol)
        }

    }

    Test fun testRoutes() {
        isolatedTest {
            val server = ServerBuilder {
                listenOn {
                    port = 8080
                }
                route("/", FileResourceHandler("/some/path")) {

                }
            }
        }
    }

}