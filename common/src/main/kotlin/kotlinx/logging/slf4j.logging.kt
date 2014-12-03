package kotlinx.logging

import org.slf4j.Logger

public inline fun Logger.debug(foo: () -> String): Unit = if (this.isDebugEnabled()) this.debug(foo())

