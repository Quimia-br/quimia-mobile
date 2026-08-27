package com.quimia.android.utils

import android.util.Log

object AnalyticsLogger {
    private const val TAG = "QUIMIA_ANALYTICS"

    fun logEvent(event: String, message: String = "", data: Map<String, String>? = null) {
        val formattedMessage = buildString {
            append("[EVENT: $event] ")
            append(message)
            data?.forEach { (key, value) ->
                append(" | $key=$value")
            }
        }
        Log.d(TAG, formattedMessage)
    }

    fun logError(event: String, exception: Exception, context: String = "") {
        Log.e(TAG, "[ERROR: $event] $context - ${exception.message}", exception)
    }

    fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    fun logWarning(message: String) {
        Log.w(TAG, message)
    }

    fun logLoginEvent(source: String, status: String, details: String = "") {
        logEvent(
            "LOGIN_ATTEMPT",
            "$source - $status - $details",
            mapOf(
                "source" to source,
                "status" to status,
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    fun logGoogleSignInEvent(step: String, details: String = "") {
        logEvent(
            "GOOGLE_SIGNIN",
            "$step: $details",
            mapOf(
                "step" to step,
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    fun logGoogleSignInError(step: String, exception: Exception) {
        logError("GOOGLE_SIGNIN", exception, "Step: $step")
    }
}
