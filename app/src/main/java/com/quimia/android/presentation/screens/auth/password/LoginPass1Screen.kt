package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import input.QuimiaInput
import theme.Typography
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun LoginPass1Screen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var email by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50.dp))
            .background(tokens.surfaceBackground)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PasswordHeader(tokens = tokens, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(34.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Esqueceu sua senha?",
                    style = Typography.titleLarge.copy(
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = tokens.textPrimary
                )
                Text(
                    text = "Digite o e-mail vinculado à sua conta para redefini-la.",
                    style = Typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    color = tokens.textSecondary
                )
            }

            QuimiaInput(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                placeholder = "exemplo@quimia.com",
                textStyle = Typography.bodyMedium.copy(fontSize = 16.sp),
                containerColor = tokens.secondary,
                primaryTextColor = tokens.textPrimary,
                secondaryTextColor = tokens.foregroundSubtle,
                radius = 1000,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 35.5.dp)
        ) {
            QuimiaButton(
                text = "Continuar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                containerColor = tokens.primaryDisabled,
                textColor = tokens.foregroundDisabled,
                onClick = onContinue,
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontSize = 20.sp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginPass1ScreenPreview() {
    QuimiaTheme {
        LoginPass1Screen()
    }
}
