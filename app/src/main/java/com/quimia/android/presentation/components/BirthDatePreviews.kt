package com.quimia.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.quimia.android.presentation.preview.QuimiaScreenPreviews
import theme.QuimiaTheme
import theme.quimiaColorTokens

@QuimiaScreenPreviews
@Preview(name = "Figma · Calendário sem seleção", group = "Nascimento", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun BirthDateEmptyPreview() = CalendarPreview("")

@QuimiaScreenPreviews
@Preview(name = "Figma · Calendário selecionado", group = "Nascimento", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun BirthDateSelectedPreview() = CalendarPreview("15/10/2026")

@Composable
private fun CalendarPreview(value: String) {
    QuimiaTheme(false) {
        Box(Modifier.fillMaxSize().background(quimiaColorTokens().surfaceBackground)) {
            BirthDateSheet(value, {}, {}, today = BirthDate(2026, 10, 26))
        }
    }
}
