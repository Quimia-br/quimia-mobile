package com.quimia.android.presentation.screens.auth

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import theme.*

class ActionColorsTest {
    @Test fun blueActionsKeepDarkTextInEveryThemeEvenWhenACallerRequestsWhite() {
        listOf(LightTokens, DarkTokens, HighContrastTokens).forEach { tokens ->
            listOf(tokens.primary, tokens.primaryPressed, tokens.primaryDisabled, tokens.brandSecondary).forEach { background ->
                assertEquals(LightTokens.foregroundPrimary, actionContentColor(background, Color.White, tokens))
                assertEquals(LightTokens.foregroundPrimary, actionContentColor(background, null, tokens))
            }
        }
    }
    @Test fun grayAndDestructiveActionsRetainTheirOwnForegroundColors() {
        assertEquals(DarkTokens.foregroundPrimary, actionContentColor(DarkTokens.secondary, null, DarkTokens))
        assertEquals(Color.White, actionContentColor(DarkTokens.error, Color.White, DarkTokens))
    }
}
