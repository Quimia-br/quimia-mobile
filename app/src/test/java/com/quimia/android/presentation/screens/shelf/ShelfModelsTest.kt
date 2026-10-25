package com.quimia.android.presentation.screens.shelf

import com.quimia.android.presentation.screens.catalog.CatalogProduct
import org.junit.Assert.assertEquals
import org.junit.Test

class ShelfModelsTest {
    private val catalog = listOf(
        CatalogProduct("b", "Veja", "Multiuso", 0, 0),
        CatalogProduct("a", "Omo", "Lava-roupas", 0, 0),
        CatalogProduct("c", "Squeeze", "Multiuso", 0, 0),
    )

    @Test fun removesDuplicatesAndUnknownProducts() {
        assertEquals(listOf("b", "a"), filterShelfProducts(catalog, listOf("b", "b", "missing", "a"), emptyList(), "", null).map { it.id })
    }

    @Test fun filtersSavedFavoritesOnly() {
        assertEquals(listOf("a"), filterShelfProducts(catalog, listOf("b", "a"), listOf("a", "c"), "", ShelfSort.Favorites).map { it.id })
    }

    @Test fun searchesCategoryIgnoringCaseAndSpaces() {
        assertEquals(listOf("b", "c"), filterShelfProducts(catalog, listOf("b", "a", "c"), emptyList(), "  MULTIUSO ", null).map { it.id })
    }

    @Test fun sortsAlphabeticallyOrBySavedOrder() {
        val ids = listOf("b", "a", "c")
        assertEquals(listOf("a", "c", "b"), filterShelfProducts(catalog, ids, emptyList(), "", ShelfSort.Alphabetical).map { it.id })
        assertEquals(listOf("c", "a", "b"), filterShelfProducts(catalog, ids, emptyList(), "", ShelfSort.Recent).map { it.id })
        assertEquals(ids, filterShelfProducts(catalog, ids, emptyList(), "", ShelfSort.Oldest).map { it.id })
    }

    @Test fun togglesFavoritesWithoutDuplicates() {
        assertEquals(emptyList<String>(), listOf("a").toggle("a"))
        assertEquals(listOf("a", "b"), listOf("a").toggle("b"))
    }
}
