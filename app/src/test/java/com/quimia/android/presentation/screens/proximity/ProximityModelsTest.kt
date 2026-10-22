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
        assertNotNull(address.copy(city = " ").validationError())
        assertNotNull(address.copy(number = "").validationError())
    }

    @Test fun formatsAddressWithoutEmptyComplement() {
        assertEquals("Rua Exemplo, 100 · Vila Mariana · São Paulo/SP", address.formatted())
        assertTrue(address.copy(complement = "Bloco A").formatted().endsWith(" · Bloco A"))
    }
}
