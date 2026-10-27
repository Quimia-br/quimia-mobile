package com.quimia.android.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.quimia.android.R
import input.QuimiaInput
import nav.QuimiaNav
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun AuthScreenLayout(
    modifier: Modifier = Modifier, onBack: (() -> Unit)? = null,
    bottomPadding: Dp = 32.dp, bodyVerticalPadding: Dp = 24.dp, footerHorizontalPadding: Dp = 24.dp,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val statusInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier.fillMaxSize().background(quimiaColorTokens().surfaceBackground).statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 600.dp).fillMaxSize().imePadding()) {
            val availableHeight = maxHeight
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = availableHeight),
                verticalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height((79.dp - statusInset).coerceAtLeast(0.dp)))
                    if (onBack != null) QuimiaNav(showBackButton = true, showCloseButton = false, onBackClick = onBack)
                }
                Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = bodyVerticalPadding)
                    .padding(bottom = if (availableHeight + statusInset < 820.dp) 24.dp else 0.dp), content = content)
                Column(Modifier.fillMaxWidth().padding(start = footerHorizontalPadding, end = footerHorizontalPadding, bottom = bottomPadding),
                    horizontalAlignment = Alignment.CenterHorizontally, content = footer)
            }
        }
    }
}

@Composable
internal fun AuthPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    QuimiaButton(text = text, onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(58.dp),
        textColor = theme.LightTokens.foregroundPrimary, iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16)
}

@Composable
internal fun AuthFooterLink(prefix: String, action: String, onClick: () -> Unit) {
    val tokens = quimiaColorTokens()
    Text(buildAnnotatedString {
        withStyle(SpanStyle(color = tokens.foregroundSubtle)) { append(prefix) }
        withStyle(SpanStyle(color = tokens.foregroundPrimary)) { append(action) }
    }, style = QuimiaTextStyles.medium16, modifier = Modifier
        .clickable(role = Role.Button, onClick = onClick).padding(top = 8.dp))
}

@Composable
internal fun AuthPasswordInput(value: String, onValueChange: (String) -> Unit, label: String, error: String? = null, showVisibilityWhenEmpty: Boolean = false, placeholder: String = "123456") {
    var visible by rememberSaveable { mutableStateOf(false) }
    QuimiaInput(value = value, onValueChange = onValueChange, label = label, placeholder = placeholder, error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (visible) Lucide.EyeOff else Lucide.Eye,
        trailingIconContentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
        showTrailingIconWhenEmpty = showVisibilityWhenEmpty, trailingIconSize = 16.dp, onTrailingIconClick = { visible = !visible })
}

@Composable
internal fun AuthSocialButtons(onGoogleClick: () -> Unit = {}, onEmailClick: () -> Unit) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(27.dp, Alignment.CenterHorizontally)) {
        QuimiaIconButton(icon = Lucide.Mail, iconPainter = painterResource(R.drawable.social_google), iconColor = tokens.foregroundPrimary,
            containerColor = tokens.secondary, iconSize = 17.dp,
            contentDescription = "Continuar com Google", onClick = onGoogleClick)
        QuimiaIconButton(icon = Lucide.Mail, iconPainter = painterResource(R.drawable.social_mail), iconColor = tokens.foregroundPrimary,
            containerColor = tokens.secondary, iconSize = 20.dp, contentDescription = "Continuar com e-mail", onClick = onEmailClick)
    }
}
