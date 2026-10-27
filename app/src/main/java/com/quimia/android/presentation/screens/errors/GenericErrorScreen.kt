package com.quimia.android.presentation.screens.errors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import feedback.QuimiaFeedbackItem
import nav.QuimiaNav
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
fun GenericErrorScreen(
    kind: GenericErrorKind,
    onClose: () -> Unit,
    onAction: (GenericErrorAction) -> Unit,
    modifier: Modifier = Modifier,
    descriptionOverride: String? = null,
) {
    val tokens = quimiaColorTokens()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    BackHandler(onBack = onClose)
    Box(modifier.fillMaxSize().background(tokens.surfaceBackground), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Spacer(Modifier.height((62.dp - statusTop).coerceAtLeast(0.dp)))
            QuimiaNav(onCloseClick = onClose)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    QuimiaFeedbackItem(isSuccess = false, iconError = Lucide.TriangleAlert,
                        containerSize = 200.dp, iconSize = 41.311.dp, contentDescription = "Erro")
                }
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(kind.title, Modifier.fillMaxWidth(), style = QuimiaTextStyles.medium24,
                        color = tokens.foregroundPrimary, textAlign = TextAlign.Center)
                    Text(descriptionOverride ?: kind.description, Modifier.fillMaxWidth(), style = QuimiaTextStyles.medium16,
                        color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
                }
            }
            QuimiaButton(modifier = Modifier.fillMaxWidth().padding(24.dp).height(43.dp),
                text = kind.action.label, textStyle = QuimiaTextStyles.medium16, textColor = tokens.foregroundPrimary,
                iconSize = 0, espacamento = 0, onClick = { onAction(kind.action) })
        }
    }
}
