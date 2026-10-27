package com.quimia.android.presentation.screens.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.quimia.android.presentation.screens.auth.*
import input.QuimiaInput
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens
import title.QuimiaMainTitle

@Composable
fun Login1Screen(
    modifier: Modifier = Modifier, onContinue: (String, String) -> Unit = { _, _ -> },
    onCreateAccount: () -> Unit = {}, onForgotPassword: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val emailFocus = remember { FocusRequester() }
    val emailError = authEmailError(email)
    val passwordError = authPasswordError(password)
    AuthScreenLayout(modifier, bottomPadding = 50.dp, bodyVerticalPadding = 0.dp, footerHorizontalPadding = 35.5.dp, footer = {
        AuthPrimaryButton("Continuar", { onContinue(email.trim(), password) }, enabled = emailError == null && passwordError == null)
        Spacer(Modifier.height(8.dp))
        AuthFooterLink("Não tem conta no Quimia? ", "Crie já", onCreateAccount)
    }) {
        QuimiaMainTitle(title = "Seja bem-vindo de volta!\nFaça login abaixo")
        Spacer(Modifier.height(34.dp))
        QuimiaInput(email, { email = it }, label = "E-mail", placeholder = "exemplo@quimia.com",
            modifier = Modifier.focusRequester(emailFocus), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            error = emailError.takeIf { email.isNotEmpty() })
        Spacer(Modifier.height(18.dp))
        AuthPasswordInput(password, { password = it }, "Senha", passwordError.takeIf { password.isNotEmpty() })
        Text("Esqueci a senha", color = quimiaColorTokens().foregroundPrimary, style = QuimiaTextStyles.medium16,
            modifier = Modifier.clickable(role = Role.Button, onClick = onForgotPassword).padding(top = 8.dp))
        Spacer(Modifier.height(56.dp))
        AuthSocialButtons(onGoogleClick = onGoogleClick) { emailFocus.requestFocus() }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun Login1ScreenPreview() { QuimiaTheme { Login1Screen() } }
