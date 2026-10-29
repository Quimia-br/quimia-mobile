package com.quimia.android.presentation.screens.errors

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiErrorTest {
    private fun http(code: Int) = HttpException(Response.error<Unit>(code, "{}".toResponseBody()))
    @Test fun `HTTP failures select the correct screen and action`() {
        mapOf(401 to GenericErrorKind.SessionExpired, 403 to GenericErrorKind.AccessDenied,
            404 to GenericErrorKind.ContentUnavailable, 410 to GenericErrorKind.ContentUnavailable,
            408 to GenericErrorKind.Timeout, 504 to GenericErrorKind.Timeout,
            429 to GenericErrorKind.ServiceUnavailable, 500 to GenericErrorKind.ServiceUnavailable,
            503 to GenericErrorKind.ServiceUnavailable, 422 to GenericErrorKind.Unknown).forEach { (code, expected) ->
            assertEquals(expected, apiErrorKind(http(code)))
        }
    }
    @Test fun `malformed JSON is a service failure and never an empty success`() {
        assertEquals(GenericErrorKind.ServiceUnavailable, apiErrorKind(SerializationException("bad JSON")))
        assertEquals(GenericErrorKind.Offline, apiErrorKind(IOException()))
        assertEquals(GenericErrorKind.Timeout, apiErrorKind(SocketTimeoutException()))
    }
    @Test fun `session expiry has precedence and cancellation is not a user error`() {
        assertEquals(GenericErrorKind.SessionExpired, apiErrorKind(IOException(), true))
        assertTrue(runCatching { apiErrorKind(CancellationException()) }.exceptionOrNull() is CancellationException)
    }
}
