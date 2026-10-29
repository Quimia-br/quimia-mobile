package com.quimia.android.presentation.screens.errors

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

/** Classifies failures only; it never retries a request or turns a failure into empty content. */
internal fun apiErrorKind(error: Throwable, signedOut: Boolean = false): GenericErrorKind {
    if (error is CancellationException) throw error
    if (signedOut) return GenericErrorKind.SessionExpired
    return when (error) {
        is HttpException -> when (error.code()) {
            401 -> GenericErrorKind.SessionExpired
            403 -> GenericErrorKind.AccessDenied
            404, 410 -> GenericErrorKind.ContentUnavailable
            408, 504 -> GenericErrorKind.Timeout
            429 -> GenericErrorKind.ServiceUnavailable
            in 500..599 -> GenericErrorKind.ServiceUnavailable
            else -> GenericErrorKind.Unknown
        }
        is SocketTimeoutException, is InterruptedIOException -> GenericErrorKind.Timeout
        is SerializationException -> GenericErrorKind.ServiceUnavailable
        is IOException -> GenericErrorKind.Offline
        else -> GenericErrorKind.Unknown
    }
}
