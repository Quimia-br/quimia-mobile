package com.quimia.android.presentation.screens.profile

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import menuBar.resolveQuimiaMenuBarIconColor
import org.junit.Assert.*
import org.junit.Test
import theme.DarkTokens
import theme.HighContrastTokens
import theme.LightTokens
import theme.resolveQuimiaContrastBorder

class GlobalContrastTest {
    @Test fun sharedComponentsHaveGreenThreeDpBordersOnlyInHighContrast() {
        assertNull(resolveQuimiaContrastBorder(false, LightTokens))
        assertNull(resolveQuimiaContrastBorder(false, DarkTokens))
        val border = requireNotNull(resolveQuimiaContrastBorder(true, HighContrastTokens))
        assertEquals(3.dp, border.width)
        assertEquals(SolidColor(HighContrastTokens.borderFocus), border.brush)
    }

    @Test fun darkAndContrastMenuIconsAreWhite() {
        val dark = resolveQuimiaMenuBarIconColor(DarkTokens, false)
        val contrast = resolveQuimiaMenuBarIconColor(HighContrastTokens, true)
        assertEquals(dark, contrast)
        assertEquals(HighContrastTokens.white, contrast)
        assertNotEquals(HighContrastTokens.secondary, contrast)
    }

    @Test fun lightMenuRemainsGrayAndDarkMenuUsesWhite() {
        assertEquals(LightTokens.foregroundSubtle, resolveQuimiaMenuBarIconColor(LightTokens, false))
        assertEquals(DarkTokens.white, resolveQuimiaMenuBarIconColor(DarkTokens, false))
    }
}
