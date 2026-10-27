package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.quimia.android.presentation.screens.auth.AuthPrimaryButton
import com.quimia.android.presentation.screens.auth.AuthScreenLayout
import input.QuimiaOtpInput
import kotlinx.coroutines.delay
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens
import title.QuimiaMainTitle

@Composable
fun LoginPassCodeScreen(
    modifier: Modifier = Modifier, email: String = "", onBack: () -> Unit = {},
    onResend: () -> Unit = {}, onConfirm: (String) -> Unit = {}, error: String? = null,
) {
    var code by rememberSaveable(email) { mutableStateOf("") }
    var seconds by rememberSaveable(email) { mutableIntStateOf(15) }
    LaunchedEffect(seconds) { if (seconds > 0) { delay(1000); seconds-- } }
    val tokens = quimiaColorTokens()
    AuthScreenLayout(modifier, onBack = onBack, footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuimiaButton(text = if (seconds > 0) "${seconds}s Reenviar" else "Reenviar",
                modifier = Modifier.weight(1f).height(58.dp), containerColor = tokens.secondary,
                enabled = seconds == 0, onClick = { onResend(); seconds = 15 }, iconSize = 0,
                espacamento = 0, textStyle = QuimiaTextStyles.medium16)
            AuthPrimaryButton("Confirmar", { onConfirm(code) }, enabled = code.length == 4, modifier = Modifier.weight(1f))
        }
    }) {
        QuimiaMainTitle(title = if (email.isNotBlank()) "Enviamos um código para\n$email" else "Digite seu código de recuperação",
            description = "Digite o código para resetar sua senha.")
        Spacer(Modifier.height(34.dp))
        QuimiaOtpInput(code, { code = it }, itemWidth = 77.dp, itemHeight = 123.dp, itemSpacing = 4.dp,
            radius = 45, textMargin = 0, error = error)
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun LoginPassCodeScreenPreview() { QuimiaTheme { LoginPassCodeScreen(email = "exemplo@quimia.com") } }
