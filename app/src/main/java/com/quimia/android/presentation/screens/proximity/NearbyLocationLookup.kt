package com.quimia.android.presentation.screens.proximity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

internal suspend fun <T> lookupNearbyLocation(
    providers: List<String>, cached: suspend (String) -> T?, current: suspend (String) -> T?,
    valid: (T) -> Boolean, providerTimeoutMillis: Long = 4_000L,
): T? {
    suspend fun attempt(provider: String, lookup: suspend (String) -> T?): T? = try {
        lookup(provider)?.takeIf(valid)
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (_: SecurityException) { null
    } catch (_: IllegalArgumentException) { null }
    for (provider in providers) attempt(provider, cached)?.let { return it }
    for (provider in providers) {
        withTimeoutOrNull(providerTimeoutMillis) { attempt(provider, current) }?.let { return it }
    }
    return null
}
