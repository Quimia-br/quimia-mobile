package com.quimia.android.presentation.screens.proximity

import org.junit.Assert.*
import org.junit.Test

class ProximityModelsTest {
    private val address = NearbyAddress("04001-000", "SP", "São Paulo", "Vila Mariana", "Rua Exemplo", "100", "")

    @Test fun filtersCategoryAndText() {
        assertEquals(listOf(4), filterNearbyPoints(nearbyPoints, NearbyCategory.Shopping).map { it.id })
        assertEquals(listOf(2), filterNearbyPoints(nearbyPoints, NearbyCategory.Collection, " COOPERATIVA ").map { it.id })
        assertTrue(filterNearbyPoints(nearbyPoints, null, "sem resultado").isEmpty())
    }

    @Test fun togglesSavedPointWithoutDuplicates() {
        assertEquals(listOf(1, 2), toggleSavedPoint(listOf(1), 2))
        assertEquals(emptyList<Int>(), toggleSavedPoint(listOf(1, 1), 1))
    }

    @Test fun validatesAddressFields() {
        assertNull(address.validationError())
        assertNotNull(address.copy(postalCode = "12").validationError())
        assertNotNull(address.copy(state = "São Paulo").validationError())
        assertNotNull(address.copy(state = "ZZ").validationError())
        assertNull(address.copy(street = "").validationError())
        assertNotNull(address.copy(city = " ").validationError())
        assertNotNull(address.copy(number = "").validationError())
    }

    @Test fun formatsAddressWithoutEmptyComplement() {
        assertEquals("Rua Exemplo, 100 · Vila Mariana · São Paulo/SP", address.formatted())
        assertTrue(address.copy(complement = "Bloco A").formatted().endsWith(" · Bloco A"))
    }

    @Test fun acceptsApproximateLocationWithoutFinePermission() {
        assertTrue(nearbyLocationPermission(fine = false, coarse = true))
        assertTrue(nearbyLocationPermission(fine = true, coarse = true))
        assertTrue(nearbyLocationPermission(fine = true, coarse = false))
        assertFalse(nearbyLocationPermission(fine = false, coarse = false))
    }

    @Test fun choosesEnabledProviderCompatibleWithPermission() {
        assertEquals("fused", nearbyLocationProvider(false, setOf("fused", "network", "gps")))
        assertEquals("gps", nearbyLocationProvider(true, setOf("gps", "network")))
        assertEquals("network", nearbyLocationProvider(false, setOf("gps", "network")))
        assertNull(nearbyLocationProvider(false, setOf("gps")))
        assertNull(nearbyLocationProvider(true, emptySet()))
    }

    @Test fun rejectsInvalidGeographicCoordinates() {
        assertTrue(nearbyDefaultCoordinate.isValid())
        assertTrue(NearbyCoordinate(-90.0, 180.0).isValid())
        assertFalse(NearbyCoordinate(90.1, 0.0).isValid())
        assertFalse(NearbyCoordinate(0.0, -180.1).isValid())
        assertFalse(NearbyCoordinate(Double.NaN, 0.0).isValid())
        assertFalse(NearbyCoordinate(0.0, Double.POSITIVE_INFINITY).isValid())
    }

    @Test fun samplePointsHaveValidCoordinatesAndRemainClearlyLabeled() {
        assertTrue(nearbyPoints.all { it.coordinate.isValid() && "exemplo" in it.name })
        assertEquals(nearbyPoints.size, nearbyPoints.map { it.id }.distinct().size)
    }
}
