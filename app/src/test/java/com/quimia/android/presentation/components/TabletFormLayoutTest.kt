package com.quimia.android.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.quimia.android.presentation.screens.auth.registration.Cadastro1Screen
import com.quimia.android.presentation.screens.profile.ProfilePreferences
import com.quimia.android.presentation.screens.profile.ProfileScreen
import com.quimia.android.presentation.screens.rooms.RoomForm
import com.quimia.android.presentation.screens.rooms.RoomIcon
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import motion.LocalQuimiaMotionEnabled
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import theme.QuimiaTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w600dp-h960dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabletFormLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private var observedIme = 0
    private lateinit var insetsView: android.view.View

    private fun content(block: @Composable () -> Unit) {
        compose.runOnUiThread { WindowCompat.setDecorFitsSystemWindows(compose.activity.window, false) }
        compose.setContent { QuimiaTheme(true) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f), LocalQuimiaMotionEnabled provides false) {
                val ime = WindowInsets.ime.getBottom(LocalDensity.current)
                val view = LocalView.current
                SideEffect { observedIme = ime; insetsView = view }
                block()
            }
        } }
    }

    private fun showKeyboard() {
        compose.runOnUiThread {
            ViewCompat.dispatchApplyWindowInsets(insetsView,
                WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 360))
                    .setVisible(WindowInsetsCompat.Type.ime(), true).build())
        }
        compose.waitForIdle()
        assertEquals("The test must actually exercise keyboard insets", 360, observedIme)
    }

    private fun capture(name: String) {
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val directory = File("build/reports/tablet-forms").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun registrationKeepsTheWholePasswordAndErrorAboveTheFixedButton() {
        content { Cadastro1Screen() }
        compose.onAllNodes(hasSetTextAction()).onLast().performScrollTo().performClick().performTextInput("123")
        showKeyboard()
        val error = compose.onNodeWithText("Use pelo menos 8 caracteres, com letra e número.").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val button = compose.onNodeWithText("Continuar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("The footer must not cover the password validation", error.bottom + 8 <= button.top)
        compose.onNodeWithContentDescription("Continuar com Google").assertDoesNotExist()
        capture("registration-keyboard-tablet")
    }

    @Test fun profileNameAndSaveStayVisibleWhileEditingOnTablet() {
        content { ProfileScreen("Pessoa teste", "teste@example.com", ProfilePreferences(), {}, {}, {}, {},
            initialEditVisible = true, birthDate = "14/11/1995") }
        compose.onAllNodes(hasSetTextAction() and isEnabled()).onFirst().performScrollTo().performClick()
        showKeyboard()
        compose.onNodeWithText("Salvar").assertIsDisplayed()
        val field = compose.onAllNodes(hasSetTextAction() and isEnabled()).onFirst().assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val button = compose.onNodeWithText("Salvar").fetchSemanticsNode().boundsInRoot
        assertTrue(field.bottom < button.top)
        capture("profile-keyboard-tablet")
    }

    @Test fun roomFormKeepsSaveFixedAndItsNameVisibleWithLargerText() {
        content { RoomForm("Cozinha", RoomIcon.Refrigerator, emptyList(), sampleCatalogProducts, null,
            {}, {}, {}, {}, {}, {}, canSubmit = true) }
        compose.onNode(hasSetTextAction()).performScrollTo().performClick()
        showKeyboard()
        compose.onNodeWithText("Criar cômodo").assertIsDisplayed()
        val field = compose.onNode(hasSetTextAction()).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val button = compose.onNodeWithText("Criar cômodo").fetchSemanticsNode().boundsInRoot
        assertTrue(field.bottom < button.top)
        capture("room-keyboard-tablet")
    }
}
