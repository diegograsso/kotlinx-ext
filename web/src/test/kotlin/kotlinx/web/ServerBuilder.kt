package kotlinx.web

import kotlinx.flow.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse


public class ServerBuilderTests {
    Test fun testListeners() {
        isolatedTest {
            val server = ServerBuilder {
                listen {
                    port = 8443
                    host = "127.0.0.1"
                    protocol = HTTPS
                }
            }

            assertEquals(1, server.allListeners.size())
            assertEquals("127.0.0.1", server.allListeners[0].host)
            assertEquals(8443, server.allListeners[0].port)
            assertEquals(ServerProtocol.HTTPS, server.allListeners[0].protocol)
        }

        isolatedTest {
            val server = ServerBuilder {
                listen {
                    port = 9080
                    host = "0.0.0.0"
                }
                listen {
                    port = 9443
                    host = "127.0.0.1"
                    protocol = HTTPS
                }
                listen {
                    port = 9092
                    host = "127.0.0.1"
                    protocol = WEBSOCKETS
                }
            }

            assertEquals(3, server.allListeners.size())

            assertEquals("0.0.0.0", server.allListeners[0].host)
            assertEquals(9080, server.allListeners[0].port)
            assertEquals(ServerProtocol.HTTP, server.allListeners[0].protocol)
            assertEquals("127.0.0.1", server.allListeners[1].host)
            assertEquals(9443, server.allListeners[1].port)
            assertEquals(ServerProtocol.HTTPS, server.allListeners[0].protocol)
            assertEquals("127.0.0.1", server.allListeners[2].host)
            assertEquals(9092, server.allListeners[2].port)
            assertEquals(ServerProtocol.WEBSOCKETS, server.allListeners[0].protocol)
        }

    }
}