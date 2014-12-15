package kotlinx.kara.cli

import kotlinx.kara.ApplicationConfig
import kotlinx.kara.server.UndertowRunner

fun server(appConfig : ApplicationConfig) {
    val runner = UndertowRunner(appConfig)
    runner.start()
}

fun config(appConfig: ApplicationConfig) {
    println(appConfig.toString())
}

fun main(args: Array<String>) {
    val appConfig = ApplicationConfig.loadFrom(if (args.size > 0) args[0] else "development.conf")

    // TODO: logging
    // val logPath = appConfig.tryGet("kara.logPropertiesPath")

    config(appConfig)
    server(appConfig)
}
