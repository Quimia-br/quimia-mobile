package com.quimia.android.presentation.screens.catalog

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test
import theme.DarkTokens
import theme.HighContrastTokens
import theme.LightTokens

class CatalogSelectionStyleTest {
    @Test fun contrastProductsAreGreenOnlyBeforeSelectionStarts() {
        assertEquals(CatalogProductOutline(HighContrastTokens.borderFocus, 3.dp),
            resolveCatalogProductOutline(HighContrastTokens, true, false, false))
        assertNull(resolveCatalogProductOutline(HighContrastTokens, true, true, false))
    }
    @Test fun selectedContrastProductsHaveBlueBordersInsteadOfGreen() {
        val selected = requireNotNull(resolveCatalogProductOutline(HighContrastTokens, true, true, true))
        assertEquals(HighContrastTokens.info, selected.color)
        assertNotEquals(HighContrastTokens.borderFocus, selected.color)
        assertEquals(3.dp, selected.width)
    }
    @Test fun cancellingSelectionRestoresTheGreenContrastBorder() {
        assertNull(resolveCatalogProductOutline(HighContrastTokens, true, true, false))
        assertEquals(HighContrastTokens.borderFocus, resolveCatalogProductOutline(HighContrastTokens, true, false, false)?.color)
    }
    @Test fun lightAndDarkSelectionRemainGreenAndUnselectedCardsStayUnoutlined() {
        listOf(LightTokens, DarkTokens).forEach { tokens ->
            assertNull(resolveCatalogProductOutline(tokens, false, false, false))
            assertNull(resolveCatalogProductOutline(tokens, false, true, false))
            assertEquals(CatalogProductOutline(tokens.success, 2.dp), resolveCatalogProductOutline(tokens, false, true, true))
        }
    }
}
