package kotlinx.kara

import javax.servlet.http.HttpServletRequest
import java.util.LinkedHashSet
import java.util.LinkedHashMap
import java.net.URLEncoder
import kotlinx.html.Link
import kotlinx.reflection.Serialization
import kotlinx.reflection.primaryProperties
import kotlinx.kara.internal.toRouteComponents
import kotlinx.kara.internal.StringRouteComponent
import kotlinx.kara.internal.OptionalParamRouteComponent
import kotlinx.kara.internal.ParamRouteComponent
import kotlinx.kara.internal.WildcardRouteComponent
import kotlinx.reflection.propertyValue
import kotlinx.kara.internal.route
import kotlinx.html.DirectLink

public abstract class Resource() : Link {
    abstract  fun handle(context: ActionContext): ActionResult

    override fun href(): String = href(contextPath())

    fun href(context: String): String {
        val url = requestParts(context)
        if (url.second.size() == 0) return url.first

        val answer = StringBuilder()

        answer.append(url.first)
        answer.append("?")
        answer.append(url.second map { "${it.key}=${Serialization.serialize(it.value)?.let{URLEncoder.encode(it, "UTF-8")}}" } join("&"))

        return answer.toString()
    }

    fun requestParts(context: String = contextPath()): Pair<String, Map<String, Any>> {
        val route = javaClass.fastRoute()

        val path = StringBuilder(context)

        val properties = LinkedHashSet(primaryProperties())
        val components = route.toRouteComponents().map({ it ->
            when (it) {
                is StringRouteComponent -> it.componentText
                is OptionalParamRouteComponent -> {
                    properties.remove(it.name)
                    Serialization.serialize(propertyValue(it.name))
                }
                is ParamRouteComponent -> {
                    properties.remove(it.name)
                    Serialization.serialize(propertyValue(it.name))
                }
                is WildcardRouteComponent -> throw RuntimeException("Routes with wildcards aren't supported")
                else -> throw RuntimeException("Unknown route component $it of class ${it.javaClass.getName()}")
            }
        })

        path.append(components.filterNotNull().join("/"))
        if (path.length() == 0) path.append("/")

        val queryArgs = LinkedHashMap<String, Any>()
        for (prop in properties filter { propertyValue(it) != null }) {
            queryArgs[prop] = propertyValue(prop)!!
        }

        return Pair(path.toString(), queryArgs)
    }
}

private fun Class<out Resource>.fastRoute(): String {
    return ActionContext.tryGet()?.application?.dispatcher?.route(this) ?: route().first
}

public fun Class<out Resource>.baseLink(): Link {
    val route = fastRoute()
    if (route.contains(":")) {
        throw RuntimeException("You can't have base link for the route with URL parameters")
    }

    return route.link()
}

public fun String.link(): Link {
    return DirectLink(appendContext())
}

public fun contextPath(): String {
    val request = ActionContext.tryGet()?.request
    if (request == null) return ""
    return request.getAttribute("CONTEXT_PATH") as? String ?: request.getContextPath() ?: ""
}

public fun HttpServletRequest.setContextPath(path: String) {
    setAttribute("CONTEXT_PATH", path)
}

public fun String.appendContext(): String {
    if (startsWith("/") && !startsWith("//")) {
        return contextPath() + this
    }

    return this
}
