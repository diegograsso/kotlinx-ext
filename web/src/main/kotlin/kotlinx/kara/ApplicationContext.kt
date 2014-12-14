package kotlinx.kara

import javax.servlet.http.*
import java.util.*
import java.io.IOException
import kotlinx.reflection.objectInstance
import kotlinx.kara.internal.ResourceDispatcher
import org.slf4j.LoggerFactory
import org.reflections.Reflections

/** Current application execution context
 */
class ApplicationContext(public val application : Application,
                         packages: List<String>,
                         val classLoader: ClassLoader,
                         val resourceTypes: List<Class<out Resource>>) {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
    private val interceptors = ArrayList<(HttpServletRequest, HttpServletResponse, (HttpServletRequest, HttpServletResponse) -> Boolean) -> Boolean>()
    private val monitorInstances = ArrayList<ApplicationContextMonitor>();

    public val version: Int = ++versionCounter;

    {
        val monitors = arrayListOf<ApplicationContextMonitor>()
        packages.flatMap { scanPackageForMonitors(it) }.forEach {
            val objectInstance = it.objectInstance()
            if (objectInstance != null) {
                monitors.add(objectInstance as ApplicationContextMonitor)
            } else {
                monitors.add(it.newInstance())
            }
        }

        for (monitor in monitors.sortBy { it.priority }) {
            logger.info("Executing startup sequence on ${monitor.javaClass}")
            monitor.created(this)
            monitorInstances.add(monitor)
        }
    }

    public fun intercept(interceptor: (request: HttpServletRequest, response: HttpServletResponse, proceed: (HttpServletRequest, HttpServletResponse) -> Boolean) -> Boolean) {
        interceptors.add(interceptor)
    }

    public fun dispatch(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        fun dispatch(index: Int, request: HttpServletRequest, response: HttpServletResponse): Boolean {
            return if (index in interceptors.indices) {
                interceptors[index](request, response) { req, resp -> dispatch(index + 1, req, resp) }
            }
            else {
                dispatcher.dispatch(request, response)
            }
        }

        try {
            return dispatch(0, request, response)
        }
        catch(ex: IOException) {
            // All kinds of EOFs and Broken Pipes can be safely ignored
        }
        catch(ex: Throwable) {
            Application.logger.error("Error processing ${request.getMethod()} ${request.getRequestURI()}. User agent: ${request.getHeader("User-Agent")}", ex)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage())
        }

        return true
    }

    fun minifyResrouces(): Boolean {
        val explicit = application.config.tryGet("kara.minifyResources")
        return when {
            explicit == "true", explicit == "yes" -> true
            explicit == "false", explicit == "no" -> false
            else -> application.config.isProduction()
        }
    }

    fun dispose() = monitorInstances.forEach { it.destroyed(this) }

    private var _dispatcher: ResourceDispatcher? = null

    public val dispatcher: ResourceDispatcher
        get() {
            var d = _dispatcher
            if (d == null) {
                d = ResourceDispatcher(this, resourceTypes)
                _dispatcher = d
            }
            return d!!
        }


    fun scanPackageForMonitors(prefix: String): List<Class<out ApplicationContextMonitor>> {
        try {
            return Reflections(prefix, classLoader).getSubTypesOf(javaClass<ApplicationContextMonitor>())!!.toList()
        }
        catch(e: Throwable) {
            // e.printStackTrace()
            return listOf<Class<ApplicationContextMonitor>>()
        }
    }

    class object {
        var versionCounter: Int = 0
    }
}
