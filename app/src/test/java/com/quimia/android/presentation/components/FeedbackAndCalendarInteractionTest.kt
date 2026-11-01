package com.quimia.android.presentation.components

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
@Config(sdk = [34], qualifiers = "w800dp-h1280dp")
class FeedbackAndCalendarInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun calendarCanSelect1900AndSaveItOnTablet() {
        var saved: String? = null
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) {
                BirthDateSheet("01/01/2000", {}, { saved = it }, today = BirthDate(2026, 11, 1))
            }
        } }
        compose.onNodeWithContentDescription("Selecionar ano").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(126)
        compose.onNodeWithText("1900").performClick()
        compose.onNodeWithContentDescription("01/01/1900").performClick()
        compose.onNodeWithText("Salvar").performClick()
        compose.runOnIdle { assertEquals("01/01/1900", saved) }
    }

    @Test fun retryAndSecondaryActionStayConnectedToTheirOriginalCallbacks() {
        var retried = 0
        var dismissed = 0
        compose.setContent { QuimiaTheme(true) {
            QuimiaFeedbackDialog("Não foi possível conferir sua sessão", { dismissed++ },
                description = "Confira a conexão com a internet e tente novamente.", kind = QuimiaFeedbackKind.Error,
                action = "Tentar novamente", onConfirm = { retried++ }, secondaryAction = "Ir para login")
        } }
        compose.onNodeWithText("Tentar novamente").assertIsDisplayed().performClick()
        compose.onNodeWithText("Ir para login").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, retried); assertEquals(1, dismissed) }
    }

    @Test fun busyConfirmationBlocksBothActions() {
        compose.setContent { QuimiaTheme(false) {
            QuimiaFeedbackDialog("Remover produto?", {}, kind = QuimiaFeedbackKind.Destructive,
                action = "Removendo…", secondaryAction = "Cancelar", busy = true)
        } }
        compose.onNodeWithText("Removendo…").assertIsNotEnabled()
        compose.onNodeWithText("Cancelar").assertIsNotEnabled()
    }
}
