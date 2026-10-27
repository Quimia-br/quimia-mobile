package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.auth.AuthPrimaryButton
import com.quimia.android.presentation.screens.auth.AuthScreenLayout
import feedback.QuimiaFeedbackItem
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens

@Composable
fun LoginPassSuccesScreen(modifier: Modifier = Modifier, onStart: () -> Unit = {}) {
    val tokens = quimiaColorTokens()
    AuthScreenLayout(modifier, bodyVerticalPadding = 0.dp, footerHorizontalPadding = 34.dp, footer = { AuthPrimaryButton("Começar", onStart) }) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(60.dp)) {
            QuimiaFeedbackItem(isSuccess = true, iconSuccess = Lucide.Check)
            Text("Senha alterada com sucesso!", style = QuimiaTextStyles.medium24.copy(fontSize = 40.sp, lineHeight = 48.sp),
                color = tokens.foregroundPrimary)
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                listOf(Lucide.Shield to "Segurança é o nosso pilar", Lucide.Puzzle to "Organização personalizada para sua casa").forEach { (icon, text) ->
                    QuimiaListItem(title = text, containerColor = tokens.surfaceBase, cornerRadius = 24.dp,
                        contentPadding = PaddingValues(16.dp), horizontalSpacing = 20.dp,
                        leadingContent = { QuimiaShapeIcon(size = 45.dp, icon = icon, iconSize = 20.dp, containerColor = tokens.secondary) })
                }
            }
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun LoginPassSuccesScreenPreview() { QuimiaTheme { LoginPassSuccesScreen() } }
