package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class EntryLoadingGateTest {
    @Test fun `page skeleton ends after first batch and never returns for a component request`() {
        val page = EntryLoadingGate()
        assertTrue(page.isLoading(true))
        page.settle(true)
        assertTrue(page.isLoading(true))
        assertFalse(page.isLoading(false))
        page.settle(false)
        // Expanding a product, changing a filter, refreshing or resuming the app cannot reset the page.
        repeat(4) {
            assertFalse(page.isLoading(true))
            page.settle(true)
            assertFalse(page.isLoading(false))
            page.settle(false)
        }
    }
    @Test fun `initial error and retry never hide the whole page again`() {
        val page = EntryLoadingGate()
        page.settle(false)
        assertFalse(page.isLoading(true))
    }
    @Test fun `entering a new page or another account starts a new loading cycle`() {
        val previousPage = EntryLoadingGate().apply { settle(false) }
        val newPage = EntryLoadingGate()
        assertFalse(previousPage.isLoading(true))
        assertTrue(newPage.isLoading(true))
    }
}
