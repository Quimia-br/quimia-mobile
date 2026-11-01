package com.quimia.android.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
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
class BirthDateEntryTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private var currentValue = ""

    private fun field(initialValue: String = "") {
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaMotionEnabled provides false) {
                var value by remember { mutableStateOf(initialValue) }
                SideEffect { currentValue = value }
                BirthDateField(value, { value = it }, error = completeBirthDateError(value))
            }
        } }
    }

    @Test fun typingUsesTheMaskAndDoesNotOpenTheCalendar() {
        field()
        compose.onNodeWithContentDescription("Data de nascimento").performClick().performTextInput("14111995")
        compose.runOnIdle { assertEquals("14/11/1995", currentValue) }
        compose.onNodeWithText("Selecionar data\nde nascimento").assertDoesNotExist()
    }

    @Test fun calendarIconOpensTheTypedDateAndSavingUpdatesTheSameField() {
        field("14/11/1995")
        compose.onNodeWithContentDescription("Abrir calendário de nascimento").performClick()
        compose.onNodeWithText("Selecionar data\nde nascimento").assertIsDisplayed()
        compose.onNodeWithContentDescription("15/11/1995").performClick()
        compose.onNodeWithText("Salvar").performClick()
        compose.runOnIdle { assertEquals("15/11/1995", currentValue) }
        compose.onNodeWithText("Selecionar data\nde nascimento").assertDoesNotExist()
    }

    @Test fun pasteClearAndMinimumYearWorkWithManualEntry() {
        field()
        val input = compose.onNodeWithContentDescription("Data de nascimento")
        input.performTextInput("01/01/0099")
        compose.onNodeWithText("O ano de nascimento deve ser 1900 ou posterior.").assertIsDisplayed()
        input.performTextReplacement("01/01/1900")
        compose.runOnIdle { assertEquals("01/01/1900", currentValue); assertNull(completeBirthDateError(currentValue)) }
        input.performTextClearance()
        compose.runOnIdle { assertEquals("", currentValue) }
        compose.onNodeWithContentDescription("Abrir calendário de nascimento").assertIsDisplayed()
    }
}
