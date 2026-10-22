package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Settings2
import listItem.QuimiaListItem
import menuBar.QuimiaMenuBar
import menuBar.QuimiaMenuBarBackdrop
import menuBar.QuimiaMenuBarBottomSpacing
import menuBar.QuimiaMenuBarHeight
import theme.QuimiaTheme
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType
import kotlinx.coroutines.delay

@Composable
fun ProximityScreen(
    modifier: Modifier = Modifier,
    onMenuItemSelected: (Int) -> Unit = {},
    initialSavedPointIds: List<Int> = emptyList(),
    onSavedPointIdsChange: (List<Int>) -> Unit = {},
    initialAddress: String = "Defina seu endereço de referência",
    onAddressChange: (String) -> Unit = {},
    initialAddressFields: List<String> = emptyList(),
    onAddressFieldsChange: (List<String>) -> Unit = {},
    previewSelectedPointId: Int? = null,
    previewSavedPoints: Boolean = false,
    previewAddressForm: Boolean = false,
    initialHeaderExpanded: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    var category by rememberSaveable { mutableStateOf<NearbyCategory?>(NearbyCategory.Collection) }
    var selectedId by rememberSaveable { mutableStateOf(previewSelectedPointId) }
    var headerExpanded by rememberSaveable { mutableStateOf(initialHeaderExpanded) }
    var savedIds by remember(initialSavedPointIds) { mutableStateOf(initialSavedPointIds.distinct()) }
    var savedVisible by rememberSaveable { mutableStateOf(previewSavedPoints) }
    var addressVisible by rememberSaveable { mutableStateOf(previewAddressForm) }
    var address by rememberSaveable(initialAddress) { mutableStateOf(initialAddress) }
    var addressFields by rememberSaveable(initialAddressFields) { mutableStateOf(initialAddressFields) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(message) { if (message != null) { delay(4_000); message = null } }
    val points = remember(category) { filterNearbyPoints(nearbyPoints, category) }
    val selected = points.find { it.id == selectedId }

    Box(modifier.fillMaxSize().background(tokens.surfaceBase)) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize()
                .statusBarsPadding().navigationBarsPadding()
                .padding(bottom = QuimiaMenuBarHeight + QuimiaMenuBarBottomSpacing + 8.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                QuimiaIconButton(icon = Lucide.ArrowLeft, size = QuimiaIconButtonSize.Small,
                    containerColor = tokens.secondary, contentDescription = "Voltar para Home", onClick = { onMenuItemSelected(0) })
            }
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 20.dp, top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Proximidade", Modifier.weight(1f), style = QuimiaTextStyles.medium22, color = tokens.textPrimary)
                QuimiaIconButton(icon = if (headerExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    containerColor = tokens.surfaceBase, size = QuimiaIconButtonSize.Small,
                    contentDescription = if (headerExpanded) "Recolher endereço" else "Mostrar endereço",
                    onClick = { headerExpanded = !headerExpanded })
            }
            if (headerExpanded) {
                Spacer(Modifier.height(12.dp))
                QuimiaListItem(
                    modifier = Modifier.padding(horizontal = 20.dp), title = "Casa", subtitle = address,
                    containerColor = tokens.surfaceBase, contentPadding = PaddingValues(14.dp),
                    trailingContent = {
                        QuimiaIconButton(icon = Lucide.Settings2, size = QuimiaIconButtonSize.Small,
                            containerColor = tokens.secondary, contentDescription = "Alterar endereço", onClick = { addressVisible = true })
                    },
                    onClick = { addressVisible = true },
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(tokens.secondary)) {
                ProximityMapPreview(points, selectedId, onPointClick = { selectedId = it })
                Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NearbyCategory.entries.forEach { item ->
                        QuimiaButton(modifier = Modifier.weight(1f).height(43.dp), text = item.label,
                            textStyle = QuimiaTextStyles.medium15, iconSize = 0, espacamento = 0,
                            containerColor = if (category == item) tokens.primary else tokens.surfaceBase,
                            textColor = tokens.textPrimary,
                            onClick = { category = if (category == item) null else item; selectedId = null })
                    }
                }
                Text("Mapa demonstrativo", Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = if (selected == null) 16.dp else 164.dp)
                    .background(tokens.surfaceBase, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp),
                    style = QuimiaTextStyles.medium11, color = tokens.textSecondary)
                if (selected != null) {
                    NearbyPointCard(
                        point = selected, saved = selected.id in savedIds,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                        onSave = {
                            val wasSaved = selected.id in savedIds
                            savedIds = toggleSavedPoint(savedIds, selected.id)
                            onSavedPointIdsChange(savedIds)
                            message = if (wasSaved) "Ponto removido dos salvos" else "Ponto salvo com sucesso"
                        },
                    )
                }
                if (message != null) QuimiaToast(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 68.dp, start = 12.dp, end = 12.dp),
                    message = message.orEmpty(), type = QuimiaToastType.Success, onDismiss = { message = null },
                )
            }
            QuimiaButton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(48.dp),
                text = "Pontos salvos (${savedIds.size})", textStyle = QuimiaTextStyles.medium16,
                iconLeftVector = Lucide.Heart, iconSize = 18, espacamento = 8,
                containerColor = tokens.surfaceBase, onClick = { savedVisible = true },
            )
        }
        QuimiaMenuBarBackdrop(Modifier.align(Alignment.BottomCenter))
        QuimiaMenuBar(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp).fillMaxWidth().navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = QuimiaMenuBarBottomSpacing),
            selectedIndex = 3, onItemSelected = onMenuItemSelected,
        )
    }
    if (savedVisible) NearbySavedPointsSheet(savedIds, onDismiss = { savedVisible = false }, onSelect = {
        selectedId = it; category = nearbyPoints.find { point -> point.id == it }?.category
        savedVisible = false
    }, onRemove = { id ->
        savedIds = savedIds.filterNot { it == id }; onSavedPointIdsChange(savedIds)
    })
    if (addressVisible) NearbyAddressSheet(
        initialAddress = addressFields.takeIf { it.size == 7 }?.let { NearbyAddress(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) },
        onDismiss = { addressVisible = false }, onSave = {
        addressFields = listOf(it.postalCode, it.state, it.city, it.district, it.street, it.number, it.complement)
        onAddressFieldsChange(addressFields)
        address = it.formatted(); onAddressChange(address); addressVisible = false
        message = "Endereço de referência atualizado"
    })
}

@Composable
internal fun NearbyPointCard(point: NearbyPoint, saved: Boolean, modifier: Modifier = Modifier, onSave: () -> Unit) {
    val tokens = quimiaColorTokens()
    QuimiaCard(
        title = point.name, description = "${point.category.label} · ${point.distance}\n${point.address}",
        primaryActionLabel = null, secondaryActionLabel = null, modifier = modifier,
        contentPadding = 16.dp, sectionSpacing = 12.dp, endIcon = null,
        titleTextStyle = QuimiaTextStyles.medium16, descriptionTextStyle = QuimiaTextStyles.medium12,
    ) {
        QuimiaButton(modifier = Modifier.fillMaxWidth().height(43.dp), text = if (saved) "Remover dos salvos" else "Salvar ponto",
            textStyle = QuimiaTextStyles.medium15, iconLeftVector = if (saved) Lucide.Check else Lucide.MapPin,
            iconSize = 18, espacamento = 8, containerColor = if (saved) tokens.secondary else tokens.primary, onClick = onSave)
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Proximidade", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Preview(name = "Proximidade · tablet paisagem", group = "Tablet QA", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Composable
private fun ProximityScreenPreview() { QuimiaTheme(false) { ProximityScreen() } }

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "GitHub · Proximidade · ponto selecionado", group = "GitHub Screens", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun ProximitySelectedPreview() {
    QuimiaTheme(false) { ProximityScreen(previewSelectedPointId = 1, initialSavedPointIds = listOf(2), initialHeaderExpanded = false) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Pontos salvos", group = "Figma", widthDp = 360, heightDp = 805)
@Composable
private fun ProximitySavedPreview() {
    QuimiaTheme(false) { ProximityScreen(initialSavedPointIds = listOf(1, 2), previewSavedPoints = true) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Alterar endereço", group = "Figma", widthDp = 360, heightDp = 805)
@Preview(name = "Endereço · fonte ampliada", group = "Acessibilidade", widthDp = 320, heightDp = 720, fontScale = 1.5f)
@Composable
private fun ProximityAddressPreview() { QuimiaTheme(false) { ProximityScreen(previewAddressForm = true) } }
