package kotlinx.jdk.uri

import kotlin.test.assertEquals
import org.junit.Test

class UriTests {

    Test fun basicUriWithParams() {
        val myUri = UriBuilder {
            scheme = "http"
            port = 80
            host = "www.ibm.com"
            params {
                +Pair("this", "that")
                +("this" to "other")
                +("kill" to "me")
                -"kill"
            }
        }
        assertEquals("http://www.ibm.com:80?this=that&this=other", myUri.toString())

        assertEquals("http://www.ibm.com:80?this=that&this=other", buildHttpUri("www.ibm.com", 80).withParams("this" to "that", "this" to "other", "kill" to "me").removeParam("kill").toString())
    }


    Test fun basicUriNoParams() {
        val myUri = UriBuilder {
            scheme = "http"
            host = "www.ibm.com"
        }
        assertEquals("http://www.ibm.com", myUri.toString())

        assertEquals("http://www.ibm.com", buildHttpUri("www.ibm.com").toString())

        assertEquals("http://localhost:8080", buildHttpUri("localhost", 8080).toString())

        assertEquals("https://www.ibm.com", buildHttpsUri("www.ibm.com").toString())

        assertEquals("https://www.ibm.com:443", buildHttpsUri("www.ibm.com", 443).toString())

        assertEquals("https://localhost:8443", buildHttpsUri("localhost", 8443).toString())
    }

    Test fun basicUriWithPath() {
        val myUri = UriBuilder {
            scheme = "http"
            host = "www.ibm.com"
            path = "/ads/server"
        }
        assertEquals("http://www.ibm.com/ads/server", myUri.toString())

        assertEquals("http://www.ibm.com/ads/server", buildHttpUri("www.ibm.com", "/ads/server").toString())
    }

    Test fun basicUriWithPathAndFragment() {
        val myUri = UriBuilder {
            scheme = "http"
            host = "www.ibm.com"
            path = "/ads/server"
            fragment = "123"
        }
        assertEquals("http://www.ibm.com/ads/server#123", myUri.toString())

        assertEquals("http://www.ibm.com/ads/server#123", buildHttpUri("www.ibm.com", "/ads/server", "123").toString())
    }

    Test fun basicUriWithIncorrectPathAndBadFragment() {
        val myUri = UriBuilder {
            scheme = "http"
            host = "www.ibm.com"
            path = "ads/server"  // missing leading /
            fragment = "#123"    // extra # at start
        }
        assertEquals("http://www.ibm.com/ads/server#123", myUri.toString())

        assertEquals("http://www.ibm.com/ads/server#123", buildHttpUri("www.ibm.com", "ads/server", "#123").toString())
    }
}
