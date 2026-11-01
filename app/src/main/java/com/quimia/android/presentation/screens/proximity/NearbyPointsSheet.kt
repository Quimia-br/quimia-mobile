package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import chip.QuimiaChip
import com.composables.icons.lucide.*
import com.quimia.android.R
import com.quimia.android.presentation.components.rememberVoiceInput
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.system.HideSystemNavigationBar
import header.QuimiaSearchBar
import listItem.QuimiaListItem
import states.QuimiaEmptyState
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder
import toast.QuimiaToastHost
import toast.QuimiaToastType

internal data class NearbyPointListEntry(
    val id: Int, val name: String, val address: String, val category: String,
    val saved: Boolean, val canOpenMap: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NearbyPointsSheet(
    points: List<NearbyPointListEntry>, initialCategory: String?, savedOnly: Boolean = true,
    loading: Boolean = false, error: String? = null, canToggleSaved: Boolean = true,
    hasMore: Boolean = false, onMore: () -> Unit = {}, onCategory: (String) -> Unit = {},
    onShowSaved: () -> Unit = {}, onDismiss: () -> Unit, onSelect: (Int) -> Unit,
    onOpenMap: (Int) -> Unit, onToggleSaved: (Int) -> Unit, onErrorDismiss: () -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    val voice = rememberVoiceInput(prompt = "Pesquise um ponto salvo",
        onUnavailable = { voiceError = "Entrada por voz indisponível neste aparelho." }) { query = it }
    val matches = points.filter { (category == null || it.category == category) &&
        (it.name.contains(query.trim(), true) || it.address.contains(query.trim(), true)) }
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(start = 17.dp, end = 17.dp, bottom = 8.dp),
        sheetMaxWidth = com.quimia.android.presentation.components.quimiaOverlayWidth(560.dp, 840.dp), shape = RoundedCornerShape(40.dp), containerColor = tokens.secondary,
        scrimColor = CatalogModalScrimColor, dragHandle = null,
    ) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(horizontal = 28.dp)) {
            Box(Modifier.fillMaxWidth().height(43.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(90.dp).height(3.dp).background(tokens.white, CircleShape))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (savedOnly) "Pontos salvos" else "Pontos próximos", Modifier.weight(1f),
                    style = QuimiaTextStyles.medium20, color = tokens.foregroundPrimary)
                QuimiaIconButton(icon = Lucide.X, containerColor = tokens.surfaceBase,
                    contentDescription = "Fechar pontos salvos", onClick = onDismiss)
            }
            Spacer(Modifier.height(28.dp))
            QuimiaSearchBar(query, { query = it }, placeholder = "Pesquisar", icon = Lucide.Mic,
                iconDescription = "Pesquisar por voz", onClick = voice, modifier = Modifier.fillMaxWidth(),
                spacedBy = 10.dp, verticalPadding = 10.dp, textStyle = QuimiaTextStyles.medium14)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("descarte" to "Descarte", "compra" to "Compra").forEach { (value, label) ->
                    QuimiaChip(label, selected = category == value, centerContent = true, modifier = Modifier.weight(1f),
                        onClick = {
                            category = if (savedOnly && category == value) null else value
                            if (!savedOnly) onCategory(value)
                        })
                }
            }
            if (!savedOnly) TextButton(onClick = onShowSaved) { Text("Ver pontos salvos", color = tokens.foregroundPrimary) }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (loading) item { QuimiaToastHost("Carregando pontos…", type = QuimiaToastType.Loading, compactLayout = true) }
                if (voiceError != null || error != null) item {
                    QuimiaToastHost(voiceError ?: error, type = QuimiaToastType.Error,
                        onDismiss = { voiceError = null; onErrorDismiss() })
                }
                if (!loading && matches.isEmpty()) item {
                    QuimiaEmptyState(icon = Lucide.MapPin, fillAvailableSpace = false,
                        title = if (query.isNotBlank()) "Nenhum ponto encontrado" else "Nenhum ponto salvo",
                        description = if (query.isNotBlank()) "Tente pesquisar outro nome ou endereço." else "Salve pontos no mapa para encontrá-los aqui.")
                }
                items(matches, key = { it.id }) { point ->
                    Surface(shape = RoundedCornerShape(32.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuimiaListItem(title = point.name, subtitle = point.address, titleMaxLines = 1, subtitleMaxLines = 1,
                                contentPadding = PaddingValues(0.dp), containerColor = Color.Transparent, cornerRadius = 0.dp,
                                trailingContent = {
                                    QuimiaIconButton(icon = Lucide.Heart,
                                        modifier = Modifier.size(45.dp).border(2.dp, Color(0xFFFF93D7), CircleShape),
                                        containerColor = tokens.surfaceBase, iconColor = if (point.saved) Color.Unspecified else Color(0xFFFF93D7),
                                        iconPainter = if (point.saved) painterResource(R.drawable.figma_heart_filled) else null,
                                        enabled = canToggleSaved, contentDescription = if (point.saved) "Remover dos salvos" else "Salvar ponto",
                                        onClick = { onToggleSaved(point.id) })
                                }, onClick = { onSelect(point.id) })
                            QuimiaButton(text = "Abrir aplicativo de mapa", iconSize = 0, espacamento = 0,
                                modifier = Modifier.fillMaxWidth().height(41.dp), textStyle = QuimiaTextStyles.medium14,
                                containerColor = tokens.secondary, textColor = tokens.foregroundPrimary,
                                enabled = point.canOpenMap, onClick = { onOpenMap(point.id) })
                        }
                    }
                }
                if (hasMore && !loading) item {
                    QuimiaButton(text = "Carregar mais", iconSize = 0, espacamento = 0, onClick = onMore)
                }
            }
        }
    }
}
