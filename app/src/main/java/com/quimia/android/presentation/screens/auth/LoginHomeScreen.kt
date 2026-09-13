package com.quimia.android.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import listItem.QuimiaListItem
import theme.QuimiaTheme
import theme.Typography

@Composable
fun LoginHomeScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                        colors = listOf(Color.Transparent, Color(0x90000000)),
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
            Spacer(modifier = Modifier.height(140.dp))

            Column {
                Text(
                    text = "Bem-vindo ao\nQuimia!",
                    style = Typography.displayMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, bottom = 50.dp),
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuimiaListItem(
                        leftIcon = Lucide.Zap,
                        title = "Agilidade nas suas\nlimpezas",
                        modifier = Modifier.fillMaxWidth(),
                        forceDark = true
                    )
                    QuimiaListItem(
                        leftIcon = Lucide.ToolCase,
                        title = "Organização & Saúde",
                        subtitle = "Utilize a funcionalidade cómodos",
                        modifier = Modifier.fillMaxWidth(),
                        forceDark = true
                    )
                    QuimiaListItem(
                        leftIcon = Lucide.Droplet,
                        title = "Precisão",
                        subtitle = "Saiba a quantidade ideal de cada produto",
                        modifier = Modifier.fillMaxWidth(),
                        forceDark = true
                    )
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

@Preview(
    showBackground = false,
    widthDp = 393,
    heightDp = 852,
    name = "LoginHome com imagem"
)
@Composable
private fun LoginHomeScreenPreview() {
    QuimiaTheme(darkTheme = true) {
        LoginHomeScreen()
    }
}
