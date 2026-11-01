package com.quimia.android.presentation.system

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.quimia.android.presentation.screens.catalog.CatalogScreen
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import motion.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import theme.QuimiaTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
class RenderingBudgetInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun compactNavigationNeverKeepsTwoDestinationsComposed() {
        var tab by mutableIntStateOf(0)
        val active = mutableSetOf<Int>()
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(LocalQuimiaRenderingBudget provides QuimiaRenderingBudget.Compact) {
                QuimiaScreenDissolve(tab) { current ->
                    DisposableEffect(current) { active += current; onDispose { active -= current } }
                    Text("Tela $current")
                }
            }
        }
        compose.runOnIdle { assertEquals(setOf(0), active); tab = 1 }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Tela 1").assertExists()
        compose.runOnIdle { assertEquals(setOf(1), active); tab = 2 }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { assertEquals(setOf(2), active) }
    }

    @Test fun catalogOnlyComposesVisibleProductRowsAndCanStillReachTheLastOne() {
        val products = (1..40).map { sampleCatalogProducts.first().copy(id = "product-$it", name = "Produto $it") }
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) {
                CatalogScreen(products = products, initialBannerVisible = false, showFab = false)
            }
        } }
        compose.onNodeWithText("Produto 1").assertExists()
        compose.onNodeWithText("Produto 40").assertDoesNotExist()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Produto 40"))
        compose.onNodeWithText("Produto 40").assertExists()
        compose.onNodeWithContentDescription("Minha estante").assertIsDisplayed()
    }
}
