package kotlinx.kara;

import java.io.InputStream
import org.lesscss.LessCompiler
import org.lesscss.LessSource

private val compiler = LessCompiler()

public open class EmbeddedLessResource(val name: String) : CachedResource() {
    override fun content(context: ActionContext): ResourceContent {
        synchronized(this) {
            val css = compiler.compile(LessSource(ClasspathResource(name, context))) ?: error("$name can't be compiled")
            val bytes =  css.toByteArray("UTF-8")
            return ResourceContent("text/css", System.currentTimeMillis(), bytes.size) { bytes.inputStream }
        }
    }
}

public class ClasspathResource(val path: String, val context: ActionContext): org.lesscss.Resource {
    override fun exists(): Boolean {
        return context.resourceStream(path) != null
    }

    override fun lastModified(): Long {
        return System.currentTimeMillis() // TODO??
    }

    override fun getInputStream(): InputStream? {
        return context.resourceStream(path)
    }

    override fun createRelative(name: String?): org.lesscss.Resource? {
        val components = path.split('/')
        return ClasspathResource("${components.take(components.size - 1).join("/")}/$name", context)
    }

    override fun getName(): String? {
        return path.split('/').last()
    }
}

