package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quimia.android.presentation.screens.auth.*
import input.QuimiaInput
import theme.QuimiaTheme
import title.QuimiaMainTitle

@Composable
fun LoginPass1Screen(
    modifier: Modifier = Modifier, onBack: () -> Unit = {}, onContinue: () -> Unit = {},
    onEmailContinue: ((String) -> Unit)? = null,
) {
    var email by rememberSaveable { mutableStateOf("") }
    val error = authEmailError(email)
    AuthScreenLayout(modifier, onBack = onBack, footerHorizontalPadding = 35.5.dp, footer = {
        AuthPrimaryButton("Continuar", { if (onEmailContinue != null) onEmailContinue(email.trim()) else onContinue() }, enabled = error == null)
    }) {
        QuimiaMainTitle(title = "Esqueceu sua senha?", description = "Digite o e-mail vinculado à sua conta para redefini-la.")
        Spacer(Modifier.height(34.dp))
        QuimiaInput(email, { email = it }, label = "E-mail", placeholder = "exemplo@quimia.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), error = error.takeIf { email.isNotEmpty() })
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun LoginPass1ScreenPreview() { QuimiaTheme { LoginPass1Screen() } }
