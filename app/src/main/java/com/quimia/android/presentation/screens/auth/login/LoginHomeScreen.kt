package com.quimia.android.presentation.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import com.composables.icons.lucide.*
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens

@Composable
fun LoginHomeScreen(modifier: Modifier = Modifier, onStart: () -> Unit = {}) {
    val tokens = quimiaColorTokens(forceDark = true)
    Box(modifier.fillMaxSize().background(tokens.surfaceBackground), contentAlignment = Alignment.TopCenter) {
        Image(painterResource(com.quimia.designsystem.R.drawable.login_image), contentDescription = null,
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().aspectRatio(393f / 746f), contentScale = ContentScale.Crop)
        BoxWithConstraints(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding()) {
            val screenHeight = maxHeight
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = screenHeight)
                .padding(start = 34.dp, end = 34.dp, top = 32.dp, bottom = 43.dp),
                verticalArrangement = Arrangement.Bottom) {
                Text("Bem-vindo ao\nQuimia!", style = QuimiaTextStyles.medium24.copy(fontSize = 40.sp, lineHeight = 48.sp), color = tokens.white)
                Spacer(Modifier.height(53.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val benefits = listOf(
                        Triple(Lucide.Droplet, "Precisão", "Saiba a quantidade ideal de cada produto"),
                        Triple(Lucide.Bath, "Organização & Saúde", "Utilize a funcionalidade cômodos"),
                        Triple(Lucide.Zap, "Agilidade nas suas limpezas\ne no seu dia a dia", null),
                    )
                    benefits.forEach { (icon, title, subtitle) ->
                        QuimiaListItem(title = title, subtitle = subtitle, forceDark = true,
                            containerColor = tokens.surfaceBase, cornerRadius = 32.dp,
                            contentPadding = PaddingValues(16.dp), horizontalSpacing = 20.dp,
                            leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = icon, iconSize = 24.dp,
                                containerColor = tokens.surfaceBackground, iconColor = tokens.foregroundPrimary) })
                    }
                }
                Spacer(Modifier.height(53.dp))
                QuimiaButton(text = "Começar", onClick = onStart, modifier = Modifier.fillMaxWidth().height(58.dp),
                    containerColor = theme.LightTokens.secondary, textColor = theme.LightTokens.foregroundPrimary,
                    iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16)
            }
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun LoginHomeScreenPreview() { QuimiaTheme(darkTheme = true) { LoginHomeScreen() } }
