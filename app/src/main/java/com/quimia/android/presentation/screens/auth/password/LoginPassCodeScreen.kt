package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import input.QuimiaOtpInput
import theme.Typography
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun LoginPassCodeScreen(
    modifier: Modifier = Modifier,
    email: String = "gabrielabenficaq@gmail.com",
    onBack: () -> Unit = {},
    onResend: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var code by remember { mutableStateOf("") }
    val isCodeComplete = code.length == 4

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
                    text = "Enviamos um código para\n$email",
                    style = Typography.titleLarge.copy(
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = tokens.textPrimary
                )
                Text(
                    text = "Digite o código para resetar sua senha.",
                    style = Typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    color = tokens.textSecondary
                )
            }

            QuimiaOtpInput(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier.fillMaxWidth(),
                itemWidth = 77.dp,
                itemHeight = 123.dp,
                itemSpacing = 4.dp,
                length = 4,
                textStyle = Typography.titleLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Medium
                ),
                containerColor = tokens.secondary,
                textColor = tokens.textPrimary,
                radius = 45,
                textMargin = 0
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuimiaButton(
                text = "15s Reenviar",
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                containerColor = tokens.secondary,
                textColor = tokens.foregroundSecondary,
                onClick = onResend,
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontSize = 20.sp)
            )
            QuimiaButton(
                text = "Confirmar",
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                containerColor = if (isCodeComplete) tokens.primary else tokens.primaryDisabled,
                textColor = if (isCodeComplete) tokens.textPrimary else tokens.foregroundDisabled,
                onClick = { if (isCodeComplete) onConfirm(code) },
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontSize = 20.sp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginPassCodeScreenPreview() {
    QuimiaTheme {
        LoginPassCodeScreen()
    }
}
