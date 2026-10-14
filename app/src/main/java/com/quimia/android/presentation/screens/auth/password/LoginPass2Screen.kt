package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import input.QuimiaInput
import theme.Typography
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun LoginPass2Screen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50.dp))
            .background(tokens.surfaceBackground),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        PasswordHeader(tokens = tokens, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(34.dp)
        ) {
            Text(
                text = "Redefina sua senha",
                style = Typography.titleLarge.copy(
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = tokens.textPrimary
            )

            PasswordInput(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nova senha",
                showPassword = showNewPassword,
                onTogglePassword = { showNewPassword = !showNewPassword },
                tokens = tokens
            )

            PasswordInput(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar nova senha",
                showPassword = showConfirmPassword,
                onTogglePassword = { showConfirmPassword = !showConfirmPassword },
                tokens = tokens
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            QuimiaButton(
                text = "Confirmar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                containerColor = tokens.primary,
                textColor = tokens.textPrimary,
                onClick = onConfirm,
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontSize = 20.sp)
            )
        }
    }
}

@Composable
private fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    tokens: theme.QuimiaColorTokens
) {
    QuimiaInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = "••••••••",
        textStyle = Typography.bodyMedium.copy(fontSize = 16.sp),
        containerColor = tokens.secondary,
        primaryTextColor = tokens.textPrimary,
        secondaryTextColor = tokens.textSecondary,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = if (showPassword) Lucide.EyeOff else Lucide.Eye,
        trailingIconContentDescription = "Mostrar ou ocultar senha",
        onTrailingIconClick = onTogglePassword,
        radius = 1000,
        modifier = Modifier
            .fillMaxWidth()
            .height(59.dp)
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginPass2ScreenPreview() {
    QuimiaTheme {
        LoginPass2Screen()
    }
}
