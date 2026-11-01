package com.quimia.android.presentation.system

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import buttons.QuimiaFAB
import feedback.LocalQuimiaSound
import feedback.QuimiaSound
import menuBar.QuimiaMenuBar
import motion.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import theme.QuimiaTheme
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LiquidMenuInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val commits = mutableListOf<Int>()
    private val sounds = mutableListOf<QuimiaSound>()
    private var fabClicks = 0

    private fun menu(compact: Boolean = false, motion: Boolean = true, rtl: Boolean = false) {
        compose.setContent { QuimiaTheme(darkTheme = compact) {
            CompositionLocalProvider(LocalQuimiaSound provides { sounds += it }, LocalQuimiaMotionEnabled provides motion,
                LocalQuimiaRenderingBudget provides if (compact) QuimiaRenderingBudget.Compact else QuimiaRenderingBudget.Standard,
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                var tab by remember { mutableIntStateOf(0) }
                QuimiaNavigationMotion(tab) {
                    Box(Modifier.fillMaxSize()) {
                        QuimiaScreenDissolve(tab, hasNavigation = { true }, navigation = { index ->
                            QuimiaMenuBar(Modifier.align(Alignment.BottomCenter), selectedIndex = index,
                                onItemSelected = { commits += it; tab = it })
                        }) { index ->
                            QuimiaMenuBar(Modifier.align(Alignment.BottomCenter).padding(16.dp), selectedIndex = index,
                                onItemSelected = { commits += it; tab = it },
                                fab = { QuimiaFAB(onClick = { fabClicks++ }, contentDescription = "Abrir IA") })
                        }
                    }
                }
            }
        } }
    }

    private fun startDrag(from: Float = .1f, to: Float = .7f) {
        compose.onNodeWithContentDescription("Menu principal").performTouchInput {
            down(Offset(width * from, height * .5f))
            moveTo(Offset(width * to, height * .5f), delayMillis = 220)
        }
    }

    private fun capture(name: String) {
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val directory = File("build/reports/liquid-menu").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun draggingPreviewsWithoutOpeningIntermediateScreensThenCommitsOnceOnRelease() {
        menu()
        startDrag()
        compose.runOnIdle { assertTrue(commits.isEmpty()); assertTrue(sounds.isEmpty()) }
        capture("phone-drag")
        compose.onNodeWithContentDescription("Menu principal").performTouchInput { up() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(3), commits); assertEquals(listOf(QuimiaSound.Navigation), sounds) }
        compose.onNodeWithContentDescription("Pontos de descarte").assertIsDisplayed()
    }

    @Test fun cancelledDragReturnsToCurrentTabWithoutNavigationOrSound() {
        menu()
        startDrag()
        compose.onNodeWithContentDescription("Menu principal").performTouchInput { cancel() }
        compose.runOnIdle { assertTrue(commits.isEmpty()); assertTrue(sounds.isEmpty()) }
        compose.onNodeWithContentDescription("Início").assertHasClickAction()
    }

    @Test fun tapsAndFabStillWorkAndDoNotStartADrag() {
        menu()
        compose.onNodeWithContentDescription("Abrir IA").performTouchInput { click(center) }
        compose.onNodeWithContentDescription("Minha estante").performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals(1, fabClicks)
            assertEquals(listOf(2), commits)
            assertEquals(listOf(QuimiaSound.Tap, QuimiaSound.Navigation), sounds)
        }
    }

    @Test fun compactAndReducedMotionKeepDragFunctional() {
        menu(compact = true, motion = false)
        startDrag(to = .9f)
        compose.onNodeWithContentDescription("Menu principal").performTouchInput { up() }
        compose.runOnIdle { assertEquals(listOf(4), commits); assertEquals(1, sounds.size) }
        capture("compact-dark-settled")
    }

    @Test fun rightToLeftDragUsesLogicalTabOrder() {
        menu(rtl = true)
        startDrag(from = .9f, to = .3f)
        compose.onNodeWithContentDescription("Menu principal").performTouchInput { up() }
        compose.runOnIdle { assertEquals(listOf(3), commits) }
    }

    @Test
    @Config(sdk = [34], qualifiers = "w600dp-h960dp")
    fun tabletCanPreviewAndReturnToTheSameTabWithoutDuplicatingCallbacks() {
        menu()
        startDrag(to = .7f)
        compose.onNodeWithContentDescription("Menu principal").performTouchInput {
            moveTo(Offset(width * .1f, height * .5f), delayMillis = 220)
        }
        capture("tablet-drag")
        compose.onNodeWithContentDescription("Menu principal").performTouchInput { up() }
        compose.runOnIdle { assertTrue(commits.isEmpty()); assertTrue(sounds.isEmpty()) }
    }
}
