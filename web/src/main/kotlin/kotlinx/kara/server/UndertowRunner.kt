package kotlinx.kara.server

import kotlinx.kara.ApplicationConfig
import org.slf4j.LoggerFactory
import kotlinx.jdk.numbers.minimum
import io.undertow.server.handlers.accesslog.AccessLogHandler
import io.undertow.server.handlers.accesslog.AccessLogReceiver
import io.undertow.server.HttpHandler
import io.undertow.Undertow
import io.undertow.UndertowOptions
import java.net.URL
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DefaultServletConfig
import javax.servlet.DispatcherType
import io.undertow.servlet.api.MimeMapping
import io.undertow.server.handlers.resource.FileResourceManager
import io.undertow.Handlers
import kotlinx.kara.Application
import kotlinx.kara.internal.KaraServlet
import io.undertow.servlet.api.InstanceFactory
import javax.servlet.Servlet
import io.undertow.servlet.api.InstanceHandle
import kotlinx.jdk.string.mustStartWith
import io.undertow.server.handlers.resource.ResourceHandler
import java.io.File

// TODO: fixup the config so it has things we need
object cfg {
    val httpIoThreads = 0
    val httpWorkerThreads = 0
    val accessLogger = LoggerFactory.getLogger("http.access")!!
    val accessLogFormat = "combined"
    val contextPath = "/"
    val deploymentName = "kara.web"
    val accessLogEnableRequestTiming = false
}

public class UndertowRunner(val applicationConfig: ApplicationConfig) {
    val logger = LoggerFactory.getLogger(this.javaClass)!!

    val ioThreads = (if (cfg.httpIoThreads == 0) Runtime.getRuntime().availableProcessors() else cfg.httpIoThreads).minimum(2)
    val workerThreads = (if (cfg.httpWorkerThreads == 0) Runtime.getRuntime().availableProcessors() * 8 else cfg.httpWorkerThreads).minimum(8)

    val application: Application = Application.load(applicationConfig)

    //   if (application.context.dispatch(request, response)) {

    inner class KaraServletInstanceHandle: InstanceHandle<Servlet> {
        val kara: KaraServlet = KaraServlet(applicationConfig)
        override fun getInstance(): Servlet? {
            return kara
        }

        override fun release() {
            // nop
        }
    }

    public fun start() {
        val port = applicationConfig.port.toInt() // TODO: move to fixedup config
        val host = "0.0.0.0" // TODO: move to fixedup config

        val deployment = Servlets.deployment()
                .setClassLoader(javaClass.getClassLoader())  // TODO: should we do one that allows jars to be added
                .setContextPath(cfg.contextPath)
                .setDefaultServletConfig(DefaultServletConfig())
                .setDefaultEncoding("UTF-8")
                .setDeploymentName(cfg.deploymentName)
                .setEagerFilterInit(true)
           //     .addFilters(
           //             Servlets.filter("SolrRequestFilter", solrDispatchFilterClass)
           //                     .addInitParam("path-prefix", null) // do we need to set thisif context path is more than one level deep?
           //     )
                // .addFilterUrlMapping("KaraFilter", "/*", DispatcherType.REQUEST)
                .addServlets(
                        Servlets.servlet("kara.config", javaClass<KaraServlet>(), { KaraServletInstanceHandle() })
                                .addMapping("/*")
                )
                .addMimeMapping(MimeMapping(".xsl", "application/xslt+xml"))
                .addWelcomePage("index.html")

        if (applicationConfig.publicDirectories.isNotEmpty()) {
            deployment.setResourceManager(FileResourceManager(File("${applicationConfig.publicDirectories.first()}").getCanonicalFile(), 1024))
        }

        // TODO: static content not working

        val deploymentManager = Servlets.defaultContainer().addDeployment(deployment)
        deploymentManager.deploy()
        val deploymentHandler = deploymentManager.start()

        // ensure we attempt to close things nicely on termination
        Runtime.getRuntime().addShutdownHook(Thread() {
            logger.warn("Undeploying servlets...")
            deploymentManager.undeploy()
            logger.warn("Undeploy complete.")
        })

        val pathHandler = deploymentHandler // Handlers.path(?)

       // applicationConfig.publicDirectories.forEach { path ->
       //     pathHandler.addPrefixPath("/", ResourceHandler(FileResourceManager(File("./${path}"), 1024)))
       // }

        val handler = AccessLogHandler(pathHandler, object : AccessLogReceiver {
            override fun logMessage(message: String?) {
                cfg.accessLogger.info(message)
            }
        }, cfg.accessLogFormat, javaClass.getClassLoader())

        val server = Undertow.builder()
                .addHttpListener(port, host)
                .setDirectBuffers(true)
                .setHandler(handler)
                .setIoThreads(ioThreads)
                .setWorkerThreads(workerThreads)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, cfg.accessLogEnableRequestTiming)
                .build()

        server.start()
        val listeningUrl = "http://127.0.0.1:${port}"
        logger.warn("Undertow HTTP Server started, listening on ${listeningUrl}")

        // TODO: have a configured check, ping URL to check
        // URL("${listeningUrl}/#/").readBytes()
        // log.warn("!!!! SERVER READY:  Listening, and ready at ${listeningUrl} !!!!")
    }

}