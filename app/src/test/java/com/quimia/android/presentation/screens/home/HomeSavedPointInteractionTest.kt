package com.quimia.android.presentation.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import motion.LocalQuimiaMotionEnabled
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import theme.QuimiaTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
class HomeSavedPointInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun withoutSavedPointHomeKeepsFindAction() {
        var finds = 0
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) { HomeScreen(onFindDropOffClick = { finds++ }) }
        } }
        compose.onNodeWithText("Encontrar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, finds) }
    }

    @Test
    @Config(sdk = [34], qualifiers = "w600dp-h960dp")
    fun savedPointReplacesFindWithAMapThatOpensTheExactPointEvenWithoutCoordinates() {
        val opened = mutableListOf<Int>()
        var finds = 0
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) {
                HomeScreen(dropOffDescription = "Último descarte", onFindDropOffClick = { finds++ },
                    dropOffMap = { HomePointMap(99, "Último descarte", null) { opened += 99 } })
            }
        } }
        compose.onNodeWithText("Encontrar").assertDoesNotExist()
        compose.onNodeWithContentDescription("Mapa do último descarte salvo: Último descarte")
            .assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf(99), opened); assertEquals(0, finds) }
    }
}
