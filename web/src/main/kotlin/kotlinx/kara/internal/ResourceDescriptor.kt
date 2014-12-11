package kotlinx.kara.internal

import javax.servlet.http.*
import java.net.URLDecoder
import kotlin.properties.*
import kotlinx.kara.Resource
import org.slf4j.LoggerFactory
import kotlinx.kara.RouteParameters
import kotlinx.kara.InvalidRouteException
import kotlinx.kara.ApplicationContext
import kotlinx.reflection.buildBeanInstance
import kotlinx.kara.ActionContext
import kotlinx.kara.withContext

val logger = LoggerFactory.getLogger(javaClass<ResourceDescriptor>())!!

/** Contains all the information necessary to match a route and execute an action.
 */
class ResourceDescriptor(val route: String, val resourceClass: Class<out Resource>) {

    private val routeComponents = route.toRouteComponents()

    // TODO: verify optional components are all last
    private val optionalComponents by Delegates.lazy { routeComponents.filter { it is OptionalParamRouteComponent }.toList() }

    public fun matches(url: String): Boolean {
        val path = url.split("\\?")[0]
        val components = path.split("/")
        if (components.size > routeComponents.size() || components.size < routeComponents.size() - optionalComponents.size())
            return false

        for (i in components.indices) {
            val component = components[i]
            val routeComponent = routeComponents[i]
            if (!routeComponent.matches(component))
                return false
        }
        return true
    }

    public fun buildParams(request: HttpServletRequest): RouteParameters {
        val url = request.getRequestURI()?.trimLeading(request.getContextPath() ?: "")!!
        val query = request.getQueryString()
        val params = RouteParameters()

        // parse the query string
        if (query != null) {
            val queryComponents = query.split("\\&") map { URLDecoder.decode(it, "UTF-8") }
            for (component in queryComponents) {
                val nvp = component.split("=")
                if (nvp.size > 1)
                    params[nvp[0]] = nvp[1]
                else
                    params[nvp[0]] = ""
            }
        }

        // parse the route parameters
        val pathComponents = url.split("/") map { URLDecoder.decode(it, "UTF-8") }
        if (pathComponents.size < routeComponents.size() - optionalComponents.size())
            throw InvalidRouteException("URL has less components than mandatory parameters of the route")
        for (i in pathComponents.indices) {
            val component = pathComponents[i]
            val routeComponent = routeComponents[i]
            routeComponent.setParameter(params, component)
        }

        // parse the form parameters
        for (formParameterName in request.getParameterNames()) {
            val value = request.getParameter(formParameterName)!!
            params[formParameterName] = value
        }

        val contentType = request.getContentType()
        if (contentType != null) {
            if (contentType.startsWith("multipart/form-data")) {
                for (part in request.getParts()!!) {
                    if (part.getSize() < 4192) {
                        val name = part.getName()!!
                        params[name] = part.getInputStream()?.buffered()?.reader()?.readText()?:""
                    }
                }
            }
        }

        return params
    }

    fun buildRouteInstance(params: RouteParameters): Resource {
        return resourceClass.buildBeanInstance {
            params[it]
        }
    }

    /** Execute the action based on the given request and populate the response. */
    public fun exec(context: ApplicationContext, request: HttpServletRequest, response: HttpServletResponse) {
        val params = buildParams(request)
        val routeInstance = try {
            buildRouteInstance(params)
        }
        catch(e: RuntimeException) {
            throw e
        }
        catch (e: Exception) {
            throw RuntimeException("Error processing ${request.getMethod()} ${request.getRequestURI()}, parameters={${params.toString()}}, User agent: ${request.getHeader("User-Agent")}", e)
        }

        val actionContext = ActionContext(context, request, response, params)
        actionContext.withContext {
            routeInstance.handle(actionContext).writeResponse(actionContext)
        }
    }

    public override fun toString(): String {
        return "Resource<${resourceClass}> at $route"
    }

}

