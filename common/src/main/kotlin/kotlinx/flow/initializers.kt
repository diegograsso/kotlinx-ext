package kotlinx.flow

private fun <T> T.verifiedBy(verifyWith: (T) -> Unit): T {
    verifyWith(this)
    return this
}

private fun <T> T.initializedBy(initWith: (T) -> Unit): T {
    initWith(this)
    return this
}

private fun <T> T.then(initWith: (T) -> Unit): T {
    initWith(this)
    return this
}
