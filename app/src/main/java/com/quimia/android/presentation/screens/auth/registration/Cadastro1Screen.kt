package com.quimia.android.presentation.screens.auth.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quimia.android.presentation.screens.auth.*
import com.quimia.android.presentation.components.BirthDateField
import input.QuimiaInput
import theme.QuimiaTheme
import title.QuimiaMainTitle

@Composable
fun Cadastro1Screen(modifier: Modifier = Modifier, onContinue: () -> Unit = {}, onLogin: () -> Unit = {}, onGoogleClick: () -> Unit = {}) {
    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val emailFocus = remember { FocusRequester() }
    val valid = authNameError(name) == null && authBirthDateError(birthDate) == null &&
        authEmailError(email) == null && authPasswordError(password) == null
    AuthScreenLayout(modifier, bottomPadding = 50.dp, bodyVerticalPadding = 0.dp, footerHorizontalPadding = 35.5.dp, footer = {
        AuthPrimaryButton("Continuar", onContinue, enabled = valid)
        Spacer(Modifier.height(8.dp))
        AuthFooterLink("Já tem cadastro? ", "Faça login aqui", onLogin)
    }) {
        QuimiaMainTitle(title = "Seja bem-vindo!\nFaça cadastro abaixo")
        Spacer(Modifier.height(31.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            QuimiaInput(name, { name = it.take(80) }, label = "Nome", placeholder = "Daniel Filho",
                error = authNameError(name).takeIf { name.isNotEmpty() })
            BirthDateField(birthDate, { birthDate = it }, error = authBirthDateError(birthDate).takeIf { birthDate.isNotEmpty() })
            QuimiaInput(email, { email = it }, label = "E-mail", placeholder = "exemplo@quimia.com",
                modifier = Modifier.focusRequester(emailFocus), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                error = authEmailError(email).takeIf { email.isNotEmpty() })
            AuthPasswordInput(password, { password = it }, "Crie uma senha", authPasswordError(password).takeIf { password.isNotEmpty() })
        }
        Spacer(Modifier.height(58.dp))
        AuthSocialButtons(onGoogleClick = onGoogleClick) { emailFocus.requestFocus() }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun Cadastro1ScreenPreview() { QuimiaTheme { Cadastro1Screen() } }
