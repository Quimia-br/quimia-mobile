package com.quimia.android.presentation.screens.rooms

import com.quimia.android.presentation.screens.catalog.CatalogProduct
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class RoomModelsTest {
    private val room = QuimiaRoom("room-1", "Banheiro", RoomIcon.Toilet, listOf("a", "b"))

    @Test fun requiresNameIconAndSavedProduct() {
        assertNotNull(roomValidationError(" ", RoomIcon.Toilet, listOf("a"), listOf("a")))
        assertNotNull(roomValidationError("A".repeat(61), RoomIcon.Toilet, listOf("a"), listOf("a")))
        assertNotNull(roomValidationError("Banheiro", null, listOf("a"), listOf("a")))
        assertNotNull(roomValidationError("Banheiro", RoomIcon.Toilet, emptyList(), listOf("a")))
        assertNull(roomValidationError(" Banheiro ", RoomIcon.Toilet, listOf("a"), listOf("a")))
    }

    @Test fun rejectsSelectionNoLongerPresentInShelf() {
        assertNotNull(roomValidationError("Banheiro", RoomIcon.Toilet, listOf("a", "removed"), listOf("a")))
    }

    @Test fun newRoomsNeverAssumeChemicalSafety() { assertEquals(RoomSafety.NotEvaluated, room.safety) }

    @Test fun removingProductInvalidatesOldSafetyAssessment() {
        val updated = room.copy(safety = RoomSafety.Safe).withoutProduct("a")
        assertEquals(listOf("b"), updated.productIds)
        assertEquals(RoomSafety.NotEvaluated, updated.safety)
    }

    @Test fun removingLastProductLeavesRoomAvailable() {
        assertTrue(room.copy(productIds = listOf("a")).withoutProduct("a").productIds.isEmpty())
    }

    @Test fun resolvesProductsWithoutDuplicatesOrMissingIds() {
        val catalog = listOf(CatalogProduct("a", "Produto A", "Multiuso", 0, 0))
        assertEquals(listOf("a"), roomProducts(room.copy(productIds = listOf("a", "a", "missing")), catalog).map { it.id })
    }

    @Test fun searchesAndSortsRoomsWithoutMutatingOriginal() {
        val rooms = listOf(room, room.copy(id = "room-2", name = "Área externa"))
        assertEquals(listOf("room-1"), filterRooms(rooms, " BANHEI ", RoomSort.Alphabetical).map { it.id })
        assertEquals(listOf("room-2", "room-1"), filterRooms(rooms, "", RoomSort.Recent).map { it.id })
        assertEquals(rooms, filterRooms(rooms, "", RoomSort.Oldest))
    }

    @Test fun upsertingSameRoomDoesNotDuplicateAfterRecreation() {
        val updated = listOf(room).upsertRoom(room.copy(name = "Banheiro social"))
        assertEquals(1, updated.size)
        assertEquals("Banheiro social", updated.single().name)
    }

    @Test fun roomStateRoundTripsAcrossRecreation() {
        assertEquals(listOf(room), Json.decodeFromString<List<QuimiaRoom>>(Json.encodeToString(listOf(room))))
    }
}
