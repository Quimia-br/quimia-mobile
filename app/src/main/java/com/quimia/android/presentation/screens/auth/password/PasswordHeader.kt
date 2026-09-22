package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import theme.QuimiaColorTokens

@Composable
internal fun PasswordHeader(
    tokens: QuimiaColorTokens,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        Spacer(modifier = Modifier.height(79.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(77.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuimiaIconButton(
                icon = Lucide.ChevronLeft,
                onClick = onBack,
                containerColor = tokens.secondary,
                iconColor = tokens.textPrimary,
                size = QuimiaIconButtonSize.Medium,
                contentDescription = "Voltar",
            )
        }
    }
}

