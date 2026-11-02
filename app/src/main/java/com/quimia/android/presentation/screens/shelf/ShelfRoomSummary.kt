package com.quimia.android.presentation.screens.shelf

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import card.QuimiaCard
import com.composables.icons.lucide.*
import com.quimia.android.data.shelf.ShelfRoom
import com.quimia.android.presentation.screens.rooms.RoomIcon
import com.quimia.android.presentation.screens.rooms.RoomShapeIcon
import com.quimia.android.presentation.screens.rooms.apiRoomIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
internal fun ShelfRoomSummary(room: ShelfRoom, onOpen: () -> Unit) {
    val tokens = quimiaColorTokens()
    val critical = room.status == "critico"
    val color = when (room.status) {
        "critico" -> tokens.error
        "atencao" -> tokens.warning
        "seguro" -> tokens.success
        else -> tokens.foregroundSubtle
    }
    val label = when (room.status) {
        "critico" -> "Risco crítico"
        "atencao" -> "Atenção à combinação"
        "seguro" -> "Combinação avaliada como segura"
        else -> "Combinação ainda não avaliada"
    }
    val icon = when (room.status) {
        "critico", "atencao" -> Lucide.TriangleAlert
        "seguro" -> Lucide.ShieldCheck
        else -> Lucide.CircleHelp
    }
    QuimiaCard(title = room.nome, showTitle = false, showDescription = false,
        modifier = Modifier.fillMaxWidth(), containerColor = tokens.surfaceBackground,
        primaryActionLabel = "Abrir cômodo", secondaryActionLabel = null, endIcon = null,
        contentPadding = 20.dp, sectionSpacing = 20.dp, onPrimaryActionClick = onOpen,
        headerContent = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val roomIcon = apiRoomIcon(room.icone)
                RoomShapeIcon(roomIcon ?: RoomIcon.Mirror, size = 56.dp, critical = critical,
                    knownIcon = roomIcon != null, onBase = true)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(room.nome, style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                    Text(if (room.totalProdutos == 1) "1 produto neste cômodo" else "${room.totalProdutos} produtos neste cômodo",
                        style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                }
            }
        }) {
        Surface(color = color.copy(alpha = .10f), shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Text(label, style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
