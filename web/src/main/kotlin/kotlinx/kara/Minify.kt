package kotlinx.kara;

import java.io.StringWriter
import org.slf4j.LoggerFactory
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import com.yahoo.platform.yui.compressor.CssCompressor
import jj.org.mozilla.javascript.ErrorReporter
import jj.org.mozilla.javascript.EvaluatorException

fun ByteArray.minifyResource(context: ActionContext, mime: String): ByteArray {
    try {
        return when {
            !context.application.minifyResrouces() -> return this
            mime == "text/javascript" -> compressJavascript()
            mime == "text/css" -> compressCss()
            else -> this
        }
    }
    catch(e: Throwable) {
        MinifierReporter.log.warn("Minification failed", e)
        return this
    }
}

private fun ByteArray.compressJavascript(): ByteArray {
    val compressor = JavaScriptCompressor(this.inputStream.reader(), MinifierReporter)
    val answer = StringWriter()

    compressor.compress(answer, 160, true, false, true, false)

    return answer.toString().toByteArray()
}

private fun ByteArray.compressCss(): ByteArray {
    val compressor = CssCompressor(this.inputStream.reader())
    val answer = StringWriter()
    compressor.compress(answer, 160)
    return answer.toString().toByteArray()
}

object MinifierReporter : ErrorReporter {
    val log = LoggerFactory.getLogger(this.javaClass)!!

    override fun warning(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int) {
        log.warn("Minification warn: $message")
    }

    override fun error(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int) {
        log.error("Minification failed: $message")
    }

    override fun runtimeError(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int): EvaluatorException? {
        this.error(message, sourceName, line, lineSource, lineOffset)
        return EvaluatorException(message)
    }
}
