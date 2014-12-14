package kotlinx.kara.tests

import kotlin.test.*
import kotlinx.kara.tests.mock.*
import org.junit.*
import kotlinx.kara.ApplicationConfig
import kotlinx.kara.Application
import kotlinx.kara.internal.scanObjects
import kotlinx.kara.internal.ResourceDispatcher

/** Tests for dispatching routes to get action info. */
class DispatchTests() {
    Before fun setUp() {
        // TODO: logging?
        // BasicConfigurator.configure()
    }

    Test fun runDispatchTests() {
        val appConfig = ApplicationConfig.loadFrom("src/test/kotlin/kotlinx/kara/tests/test.conf")

        val app = object : Application(appConfig) {}

        val dispatcher = ResourceDispatcher(app.context, scanObjects(array(Routes)))

        var actionInfo = dispatcher.findDescriptor("GET", "/")!!
        assertNotNull(actionInfo)

        assertNull(dispatcher.findDescriptor("GET", "/invalid_path"))

        // foo controller
        actionInfo = dispatcher.findDescriptor("GET", "/foo/bar")!!
        assertEquals(Routes.Foo.Bar().javaClass, actionInfo.resourceClass)

        dispatcher.findDescriptor("GET", "/foo/bar/baz")!! // nested route

        dispatcher.findDescriptor("GET", "/foo/foobar") // default action name

        var request = mockRequest("GET", "/foo/bar/list")
        actionInfo = dispatcher.findDescriptor("GET", request.getRequestURI()!!)!! // unnamed param
        var params = actionInfo.buildParams(request)
        assertEquals("bar", params[0])

        request = mockRequest("GET", "/foo/complex/bar/list/42")
        actionInfo = dispatcher.findDescriptor("GET", request.getRequestURI()!!)!! // named and unnamed params
        params = actionInfo.buildParams(request)
        assertEquals("bar", params[0])
        assertEquals("42", params["id"])
        assertEquals(2, params.size())

        // crud controller
        request = mockRequest("GET", "/crud?name=value")
        actionInfo = dispatcher.findDescriptor("GET", request.getRequestURI()!!)!! // empty route with parameters
        assertEquals(Routes.Crud.Index().javaClass, actionInfo.resourceClass)
        params = actionInfo.buildParams(request)
        assertEquals("value", params["name"])

        request = mockRequest("GET", "/crud/42")
        actionInfo = dispatcher.findDescriptor("GET", request.getRequestURI()!!)!! // named parameter
        params = actionInfo.buildParams(request)
        assertEquals("42", params["id"])

        dispatcher.findDescriptor("POST", "/crud") // models

        request = mockRequest("PUT", "/crud/42")
        actionInfo = dispatcher.findDescriptor("PUT", request.getRequestURI()!!)!! // put
        params = actionInfo.buildParams(request)
        assertEquals("42", params["id"])

        request = mockRequest("DELETE", "/crud/42")
        actionInfo = dispatcher.findDescriptor("DELETE", request.getRequestURI()!!)!! // delete
        params = actionInfo.buildParams(request)
        assertEquals("42", params["id"])
    }
}
