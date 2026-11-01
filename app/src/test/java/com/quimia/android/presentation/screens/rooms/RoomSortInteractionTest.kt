package com.quimia.android.presentation.screens.rooms

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import motion.LocalQuimiaMotionEnabled
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import theme.QuimiaTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w600dp-h960dp")
class RoomSortInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun connectedListOffersBothDateOrdersAndReflectsTheControlledSelection() {
        val selections = mutableListOf<RoomSort>()
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) {
                var sort by remember { mutableStateOf(RoomSort.Alphabetical) }
                RoomList(emptyList(), {}, {}, {}, {}, connected = true, showEmpty = false,
                    selectedSort = sort, onSortChange = { sort = it; selections += it })
            }
        } }
        compose.onNodeWithContentDescription("Mostrar ou ocultar filtros").performClick()
        compose.onNodeWithText("Recentes").assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithText("Antigos").performClick().assertIsSelected()
        compose.onNodeWithText("Recentes").assertIsNotSelected()
        compose.onNodeWithText("Alfabética").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(listOf(RoomSort.Recent, RoomSort.Oldest, RoomSort.Alphabetical), selections) }
    }
}
