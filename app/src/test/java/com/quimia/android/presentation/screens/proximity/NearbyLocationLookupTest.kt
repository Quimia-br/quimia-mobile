package com.quimia.android.presentation.screens.proximity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NearbyLocationLookupTest {
    @Test fun approximateAccessNeverRequestsTheGpsOnlyProvider() {
        assertEquals(listOf("fused", "network"), nearbyLocationProviders(false, setOf("gps", "fused", "network")))
        assertEquals(emptyList<String>(), nearbyLocationProviders(false, setOf("gps")))
    }
    @Test fun usesAFreshCachedFixWithoutStartingAnExpensiveRequest() = runBlocking {
        val calls = mutableListOf<String>()
        val fix = lookupNearbyLocation(listOf("fused", "network"), cached = { if (it == "network") 10 else null },
            current = { calls += it; null }, valid = { it > 0 })
        assertEquals(10, fix)
        assertTrue(calls.isEmpty())
    }
    @Test fun stalledFusedProviderFallsBackToNetworkInsteadOfWaitingIndefinitely() = runBlocking {
        val calls = mutableListOf<String>()
        val fix = lookupNearbyLocation(listOf("fused", "network"), cached = { null }, current = {
            calls += it
            if (it == "fused") { delay(10_000); null } else 42
        }, valid = { it > 0 }, providerTimeoutMillis = 20)
        assertEquals(42, fix)
        assertEquals(listOf("fused", "network"), calls)
    }
    @Test fun invalidFixAndUnavailableProviderDoNotPreventFallback() = runBlocking {
        val fix = lookupNearbyLocation(listOf("fused", "network"), cached = { -1 },
            current = { if (it == "fused") throw IllegalArgumentException() else 42 }, valid = { it > 0 })
        assertEquals(42, fix)
    }
    @Test fun cancellationDoesNotStartAnotherLocationRequest() = runBlocking {
        val calls = mutableListOf<String>()
        try {
            lookupNearbyLocation<Int>(listOf("fused", "network"), cached = { null },
                current = { calls += it; throw CancellationException() }, valid = { true })
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        assertEquals(listOf("fused"), calls)
    }
    @Test fun approximateFixUsesARegionalZoomAndRejectsStaleCache() {
        assertTrue(nearbyLocationZoom(false, 0f) < nearbyLocationZoom(true, 10f))
        assertTrue(nearbyLocationZoom(false, 5_000f) <= 12f)
        assertFalse(nearbyFixIsFresh(-1))
        assertTrue(nearbyFixIsFresh(120_000))
        assertFalse(nearbyFixIsFresh(120_001))
    }
}
