package kotlinx.flow

public fun isolatedTest(scope: ()->Unit): Unit {
    scope()
}