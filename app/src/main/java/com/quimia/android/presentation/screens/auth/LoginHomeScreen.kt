package com.quimia.android.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Droplet
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ToolCase
import com.composables.icons.lucide.Zap
import theme.QuimiaTheme
import theme.Typography as QuimiaTypography
import theme.quimiaColorTokens

@Composable
fun LoginHomeScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()
    val items = listOf(
        LoginFeature(Lucide.Zap, null, "Agilidade nas suas\nlimpezas", ""),
        LoginFeature(null, Lucide.ToolCase, "Organização & Saúde", "Utilize a funcionalidade cómodos"),
        LoginFeature(Lucide.Droplet, null, "Precisão", "Saiba a quantidade ideal de cada produto")
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = com.quimia.designsystem.R.drawable.login_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x90000000)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Column {
                Text(
                    text = "Bem-vindo ao\nQuimia!",
                    style = QuimiaTypography.displayMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp),
                    fontWeight = FontWeight.Medium
                )

                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(tokens.surfaceBackground.copy(alpha = 0.65f))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(tokens.surfaceBase.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                item.imageVector?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tokens.foregroundPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                item.drawableRes?.let { icon ->
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = null,
                                        tint = tokens.foregroundPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = item.title,
                                    style = QuimiaTypography.titleMedium,
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                if (item.subtitle.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = QuimiaTypography.bodyMedium,
                                        color = tokens.textPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            QuimiaButton(
                text = "Começar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                iconSize = 20,
                containerColor = Color.White,
                textColor = Color.Black,
                onClick = onStart,
                espacamento = 0
            )
        }
    }
}

private data class LoginFeature(
    val imageVector: ImageVector?,
    val drawableRes: Int?,
    val title: String,
    val subtitle: String
)

@Preview(
    showBackground = false,
    widthDp = 393,
    heightDp = 852,
    name = "LoginHome com imagem"
)
@Composable
private fun LoginHomeScreenPreviewWithImage() {
    QuimiaTheme(darkTheme = true) {
        LoginHomeScreen()
    }
}
