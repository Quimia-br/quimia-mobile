package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import buttons.QuimiaIconButton
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import toast.QuimiaToastHost
import toast.QuimiaToastType

@Composable
internal fun NearbyNavigationHeader(loadingMessage: String? = null, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuimiaIconButton(icon = Lucide.ChevronLeft, containerColor = quimiaColorTokens().secondary,
            contentDescription = "Voltar", onClick = onBack)
        Box(Modifier.weight(1f)) {
            QuimiaToastHost(loadingMessage, type = QuimiaToastType.Loading, compactLayout = true)
        }
    }
}
