package kotlinx.kara.internal

import javax.servlet.http.*
import kotlin.properties.Delegates
import kotlinx.kara.Application
import org.slf4j.LoggerFactory
import kotlinx.kara.ApplicationConfig

open class KaraServlet(val forceConfig: ApplicationConfig? = null) : HttpServlet() {
    val logger = LoggerFactory.getLogger(javaClass<KaraServlet>())!!

    val application: Application by Delegates.blockingLazy {
        val servletContext = getServletContext()!!
        val config: ApplicationConfig = forceConfig ?: ApplicationConfig.loadFrom(servletContext.getInitParameter("kara.config") ?: error("kara.config context parameter is required."))

        for (name in servletContext.getInitParameterNames()) {
            config[name] = servletContext.getInitParameter(name)!!
        }

        Application.load(config)
    }


    override fun init() {
        super<HttpServlet>.init()
        application.config // Just to make sure application is eagerly loaded when servlet is initialized.
    }

    public override fun destroy() {
        application.shutDown()
    }

    protected override fun service(req: HttpServletRequest?, resp: HttpServletResponse?) {
        dispatch(req!!, resp!!)
    }

    private fun dispatch(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.setCharacterEncoding("UTF-8")
        req.setCharacterEncoding("UTF-8")

        try {
            val query = req.getQueryString()
            if (!application.context.dispatch(req, resp)) {
                logger.trace("${req.getMethod()} -- ${req.getRequestURL()}${if (query != null) "?" + query else ""} -- FAILED")
                resp.sendError(HttpServletResponse.SC_NOT_FOUND)
            } else {
                logger.trace("${req.getMethod()} -- ${req.getRequestURL()}${if (query != null) "?" + query else ""} -- OK")
            }
        }
        catch (ex: Throwable) {
            logger.error("Error processing request: ${req.getRequestURI()}, User agent: ${req.getHeader("User-Agent")}", ex)
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage())
            }
        }
    }

}
