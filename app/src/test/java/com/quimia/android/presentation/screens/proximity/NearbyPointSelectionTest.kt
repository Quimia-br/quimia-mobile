package com.quimia.android.presentation.screens.proximity

import com.quimia.android.data.proximity.ApiPoint
import org.junit.Assert.*
import org.junit.Test

class NearbyPointSelectionTest {
    private val lastSaved = ApiPoint(99, "Último descarte", "descarte", latitude = -23.55, longitude = -46.63)

    @Test fun selectedSavedPointIsAvailableEvenWhenItIsNotInTheFirstPage() {
        val firstPage = (1..20).map { ApiPoint(it, "Descarte $it", "descarte") }
        val visible = nearbyVisiblePoints(firstPage, listOf(lastSaved), 99, "descarte")
        assertEquals(21, visible.size)
        assertEquals(lastSaved, visible.single { it.id == 99 })
    }

    @Test fun savedPointSuppliesItsConfirmedCoordinatesWithoutDuplicateMarkers() {
        val visible = nearbyVisiblePoints(listOf(lastSaved.copy(latitude = null, longitude = null)), listOf(lastSaved), 99, "descarte")
        assertEquals(listOf(lastSaved), visible)
    }

    @Test fun unknownPointsAreNeverInventedAndOtherCategoriesDoNotLeakIntoTheMap() {
        val shop = ApiPoint(7, "Loja", "compra")
        assertEquals(emptyList<ApiPoint>(), nearbyVisiblePoints(listOf(shop), listOf(lastSaved), 99, "compra").filter { it.id == 99 })
        assertEquals(listOf(shop), nearbyVisiblePoints(listOf(shop), listOf(lastSaved), 1234, "compra"))
        assertEquals(emptyList<ApiPoint>(), nearbyVisiblePoints(emptyList(), listOf(lastSaved), null, "descarte"))
    }
}
