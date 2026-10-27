package com.quimia.android.presentation.screens.rooms

import com.quimia.android.presentation.screens.catalog.CatalogProduct
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class RoomModelsTest {
    private val room = QuimiaRoom("room-1", "Banheiro", RoomIcon.Toilet, listOf("a", "b"))

    @Test fun findsTheRoomContainingTheShelfProduct() {
        val unrelated = room.copy(id = "room-2", productIds = listOf("c"))
        assertEquals(listOf(room), roomsForProduct(listOf(unrelated, room), "a"))
    }

    @Test fun findsAllLinkedRoomsWithoutDuplicateChoices() {
        val kitchen = room.copy(id = "kitchen", name = "Cozinha", icon = RoomIcon.Refrigerator)
        assertEquals(listOf(room, kitchen), roomsForProduct(listOf(room, kitchen, room), "a"))
    }

    @Test fun unlinkedProductsHaveNoRoomChoices() {
        assertTrue(roomsForProduct(listOf(room), "missing").isEmpty())
        assertTrue(roomsForProduct(emptyList(), "a").isEmpty())
    }

    @Test fun shelfRoomLookupReflectsCreationRemovalAndRename() {
        val created = emptyList<QuimiaRoom>().upsertRoom(room)
        assertEquals(listOf(room), roomsForProduct(created, "a"))
        val renamed = created.upsertRoom(room.copy(name = "Banheiro social"))
        assertEquals("Banheiro social", roomsForProduct(renamed, "a").single().name)
        assertTrue(roomsForProduct(renamed.upsertRoom(renamed.single().withoutProduct("a")), "a").isEmpty())
    }

    @Test fun openingAnExistingRoomStartsOnItsDetails() {
        assertEquals(RoomPage.Detail, roomStartPage(null, room.id))
        assertEquals(RoomPage.List, roomStartPage(null, null))
    }

    @Test fun creatingFromShelfAndExplicitPreviewStatesArePreserved() {
        assertEquals(RoomPage.Create, roomStartPage("a", room.id))
        assertEquals(RoomPage.Success, roomStartPage(null, room.id, RoomPage.Success))
        assertEquals(RoomPage.Error, roomStartPage(null, room.id, RoomPage.Error))
    }

    @Test fun phoneGridsKeepTheTwoColumnsFromTheDesign() {
        listOf(320f, 360f, 393f, 499f).forEach { assertEquals(2, roomGridColumns(it)) }
    }

    @Test fun widerGridsUseTheirActualContainerWidth() {
        assertEquals(3, roomGridColumns(500f))
        assertEquals(3, roomGridColumns(600f))
        assertEquals(4, roomGridColumns(700f))
        assertEquals(4, roomGridColumns(840f))
    }

    @Test fun roomIconsPreserveTheirSizeAndMinimumSpacing() {
        assertEquals(4, roomIconColumns(272f))
        assertEquals(4, roomIconColumns(311f))
        assertEquals(5, roomIconColumns(312f))
        assertEquals(5, roomIconColumns(552f))
    }

    @Test fun narrowIconContainersNeverOverflowWithFourFixedIcons() {
        assertEquals(3, roomIconColumns(200f))
        assertEquals(1, roomIconColumns(56f))
        assertEquals(1, roomIconColumns(0f))
    }

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
        val restored = Json.decodeFromString<List<QuimiaRoom>>(Json.encodeToString(listOf(room)))
        assertEquals(listOf(room), restored)
        assertEquals(listOf(room), roomsForProduct(restored, "a"))
    }
}
