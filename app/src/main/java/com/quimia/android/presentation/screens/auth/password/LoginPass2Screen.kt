package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quimia.android.presentation.screens.auth.*
import theme.QuimiaTheme
import title.QuimiaMainTitle

@Composable
fun LoginPass2Screen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onConfirm: () -> Unit = {},
    onPasswordConfirm: ((String) -> Unit)? = null) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    val passwordError = authNewPasswordError(password)
    val confirmationError = authPasswordConfirmationError(password, confirmation)
    AuthScreenLayout(modifier, onBack = onBack, bodyVerticalPadding = 0.dp, bottomPadding = 24.dp, footer = {
        AuthPrimaryButton("Confirmar", { onPasswordConfirm?.invoke(password) ?: onConfirm() }, enabled = passwordError == null && confirmationError == null)
    }) {
        QuimiaMainTitle(title = "Redefina sua senha")
        Spacer(Modifier.height(34.dp))
        AuthPasswordInput(password, { password = it }, "Nova senha", passwordError.takeIf { password.isNotEmpty() }, showVisibilityWhenEmpty = true, placeholder = "••••••••")
        Spacer(Modifier.height(34.dp))
        AuthPasswordInput(confirmation, { confirmation = it }, "Confirmar nova senha", confirmationError.takeIf { confirmation.isNotEmpty() }, showVisibilityWhenEmpty = true, placeholder = "••••••••")
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun LoginPass2ScreenPreview() { QuimiaTheme { LoginPass2Screen() } }
