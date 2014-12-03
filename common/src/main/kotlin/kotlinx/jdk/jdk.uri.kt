package kotlinx.jdk.uri

import kotlin.properties.Delegates
import java.net.URI
import java.net.URL
import java.util.LinkedHashMap
import java.net.URLEncoder
import kotlinx.jdk.collections.isNotEmpty
import kotlinx.jdk.string.isNotTrimmedEmpty
import kotlinx.jdk.string.mustNotStartWith
import kotlinx.jdk.string.mustStartWith


public fun buildUri(uri: URI): UriBuilder {
    return UriBuilder {
        scheme = uri.getScheme()
        userInfo = uri.getUserInfo()
        host = uri.getHost()
        port = uri.getPort()
        path = uri.getPath()
        query.putAll(queryStringToMap(uri.getQuery()))
        fragment = uri.getFragment()
    }
}

public fun buildUri(uriString: String): UriBuilder {
    return buildUri(URI(uriString))
}

public fun buildUri(url: URL): UriBuilder {
    return buildUri(url.toURI())
}

public fun buildHttpUri(hostname: String, requestPath: String? = null, requestFragment: String? = null): UriBuilder {
    return buildHttpUri(hostname, -1, requestPath, requestFragment)
}

public fun buildHttpUri(hostname: String, hostport: Int, requestPath: String? = null, requestFragment: String? = null): UriBuilder {
    return UriBuilder {
        scheme = "http"
        port = hostport
        host = hostname
        path = requestPath
        fragment = requestFragment
    }
}

public fun buildHttpsUri(hostname: String, requestPath: String? = null, requestFragment: String? = null): UriBuilder {
    return buildHttpsUri(hostname, -1, requestPath, requestFragment)
}

public fun buildHttpsUri(hostname: String, hostport: Int, requestPath: String? = null, requestFragment: String? = null): UriBuilder {
    return UriBuilder {
        scheme = "https"
        port = hostport
        host = hostname
        path = requestPath
        fragment = requestFragment
    }
}

class UriBuilder(init: UriBuilder.() -> Unit = {}) {
    var scheme: String by Delegates.notNull()
    var userInfo: String? = null
    var host: String by Delegates.notNull()
    var port: Int? = null
    var path: String? = null
    val query: MutableMap<String, MutableList<String?>> = java.util.LinkedHashMap()
    var fragment: String? = null

    {
        with(this) { init() }
    }

    fun UriBuilder.params(init: UriParams.() -> Unit = {}): Unit {
        UriParams().init()
    }

    inner class UriParams() {
        fun Pair<String, String?>.plus() {
            this@UriBuilder.withParams(this)
        }

        fun String.minus() {
            this@UriBuilder.removeParam(this)
        }
    }


    fun replaceParams(vararg params: Pair<String, String?>): UriBuilder {
        for (param in params) {
            query.remove(param.first)
        }
        return withParams(*params)
    }

    fun withParams(vararg params: Pair<String, String?>): UriBuilder {
        for (param in params) {
            query.getOrPut(param.first, { arrayListOf() }).add(param.second)
        }
        return this
    }

    fun removeParam(key: String): UriBuilder {
        query.remove(key)
        return this
    }

    fun setPath(path: String): UriBuilder {
        this.path = path
        return this
    }

    fun setFragment(frag: String): UriBuilder {
        this.fragment = frag
        return this
    }

    fun build(): URI {
        val actualPort = port ?: -1
        val uri = URI(scheme, userInfo, host, actualPort, null, null, null)
        val sb = StringBuilder()
        if (path.isNotTrimmedEmpty()) {
            sb.append(path!!.mustStartWith('/'))
        }
        if (query.isNotEmpty()) {
            val queryString = queryMapToString(query)
            if (queryString.isNotTrimmedEmpty()) {
                sb.append('?').append(queryString!!.mustNotStartWith('?'))
            }
        }
        if (fragment.isNotTrimmedEmpty()) {
            sb.append('#').append(fragment!!.mustNotStartWith('#'))
        }
        return uri.resolve(sb.toString())
    }

    override fun toString(): String {
        return build().toString()
    }
}

private val utf8 = Charsets.UTF_8.name()

private fun queryStringToMap(queryString: String?): Map<String, MutableList<String?>> {
    val query = LinkedHashMap<String, MutableList<String?>>()
    if (queryString.isNotTrimmedEmpty()) {
        queryString?.split('&')?.forEach { keyValuePair ->
            val parts = keyValuePair.split("=", 2)
            val key = parts[0].trim()
            val value = if (parts.size() == 2) parts[1] else null
            query.getOrPut(key, { arrayListOf() }).add(value)
        }
    }
    return query
}

private fun queryMapToString(queryString: Map<String, List<String?>>): String? {
    return if (queryString.isEmpty()) {
        null
    } else {
        val parts = queryString.entrySet().map { entry ->
            val encodedKey = URLEncoder.encode(entry.key, utf8)
            entry.value.map { value -> Pair<String, String?>(encodedKey, URLEncoder.encode(value ?: "", utf8)) }
        }.flatMap { it }

        parts.map { "${it.first}=${it.second}" }.joinToString("&")
    }
}
