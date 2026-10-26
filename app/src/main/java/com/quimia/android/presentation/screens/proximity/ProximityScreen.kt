package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import chip.QuimiaChip
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.MapPinned
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import title.QuimiaSectionTitle
import theme.QuimiaTheme
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.quimia.android.data.project.ApiPoint
import com.quimia.android.data.project.ApiContractGaps
import com.quimia.android.data.project.fullAddress

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
    initialCollectionFilter: Boolean = false,
    apiPoints: List<ApiPoint>? = null,
    onSaveRemote: (suspend (NearbyAddress) -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    var category by rememberSaveable { mutableStateOf<NearbyCategory?>(if (initialCollectionFilter || previewSelectedPointId != null) NearbyCategory.Collection else null) }
    var selectedId by rememberSaveable { mutableStateOf(previewSelectedPointId) }
    var headerExpanded by rememberSaveable { mutableStateOf(initialHeaderExpanded) }
    var savedIds by remember(initialSavedPointIds) { mutableStateOf(initialSavedPointIds.distinct()) }
    var savedVisible by rememberSaveable { mutableStateOf(previewSavedPoints) }
    var addressVisible by rememberSaveable { mutableStateOf(previewAddressForm) }
    var address by rememberSaveable(initialAddress) { mutableStateOf(initialAddress) }
    var addressFields by rememberSaveable(initialAddressFields) { mutableStateOf(initialAddressFields) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var messageType by remember { mutableStateOf(QuimiaToastType.Error) }
    LaunchedEffect(message) { if (message != null) { delay(4_000); message = null } }
    val sourcePoints = if (androidx.compose.ui.platform.LocalInspectionMode.current) nearbyPoints else emptyList()
    val points = remember(category, sourcePoints) { if (category == null) emptyList() else filterNearbyPoints(sourcePoints, category) }
    val requests = rememberCoroutineScope()
    val selected = points.find { it.id == selectedId }
    val density = LocalDensity.current
    var pointCardHeight by remember { mutableStateOf(146.dp) }

    Box(modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                QuimiaIconButton(icon = Lucide.ChevronLeft, size = QuimiaIconButtonSize.Medium,
                    containerColor = tokens.secondary, contentDescription = "Voltar para Home", onClick = { onMenuItemSelected(0) })
            }
            QuimiaSectionTitle(title = "Proximidade", titleSize = 24.sp, textStyle = QuimiaTextStyles.medium24,
                horizontalPadding = 32.dp, verticalPadding = 32.dp) {
                QuimiaIconButton(icon = if (headerExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    modifier = Modifier.size(24.dp), containerColor = Color.Transparent, iconSize = 24.dp,
                    contentDescription = if (headerExpanded) "Recolher endereço" else "Mostrar endereço",
                    onClick = { headerExpanded = !headerExpanded })
            }
            if (headerExpanded) {
                QuimiaListItem(
                    modifier = Modifier.padding(horizontal = 24.dp), title = if (apiPoints != null) "Endereço cadastrado" else "Casa", subtitle = address,
                    containerColor = tokens.surfaceBase, contentPadding = PaddingValues(24.dp), titleMaxLines = 1, subtitleMaxLines = 1,
                    trailingContent = {
                        QuimiaIconButton(icon = Lucide.Pencil, size = QuimiaIconButtonSize.Medium,
                            containerColor = tokens.secondary, contentDescription = "Alterar endereço", onClick = { addressVisible = true })
                    },
                    onClick = { addressVisible = true },
                )
                Spacer(Modifier.height(16.dp))
            }
            if (apiPoints != null) Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).heightIn(max = 200.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                androidx.compose.material3.Text(ApiContractGaps.PointCoordinates, style = QuimiaTextStyles.medium12, color = tokens.textSecondary)
                apiPoints.filter { category == null || it.tipo == if (category == NearbyCategory.Shopping) "compra" else "descarte" }.forEach { point ->
                    QuimiaListItem(title = point.nome, subtitle = point.fullAddress(), contentPadding = PaddingValues(vertical = 10.dp))
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 39.dp, topEnd = 39.dp)).background(tokens.secondary)) {
                ProximityMap(points, selectedId, onPointClick = { selectedId = it },
                    bottomContentInset = if (selected == null) 91.dp else 107.dp + pointCardHeight)
                Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NearbyCategory.entries.forEach { item ->
                        QuimiaChip(modifier = Modifier.weight(1f), label = item.label, selected = category == item, centerContent = true,
                            onClick = { category = if (category == item) null else item; selectedId = null })
                    }
                }
                if (selected != null) {
                    NearbyPointCard(
                        point = selected, saved = selected.id in savedIds,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(start = 17.dp, end = 17.dp, bottom = 91.dp)
                            .onSizeChanged { pointCardHeight = with(density) { it.height.toDp() } },
                        onSave = {
                            val wasSaved = selected.id in savedIds
                            savedIds = toggleSavedPoint(savedIds, selected.id)
                            onSavedPointIdsChange(savedIds)
                            messageType = QuimiaToastType.Success
                            message = if (wasSaved) "Ponto removido dos salvos" else "Ponto salvo com sucesso"
                        },
                    )
                }
                if (message != null) QuimiaToast(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 75.dp),
                    message = message.orEmpty(), type = messageType, onDismiss = { message = null },
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 560.dp).fillMaxWidth()
                .padding(horizontal = 17.dp).height(75.dp)
                .pointerInput(apiPoints != null) { detectVerticalDragGestures { change, amount -> change.consume(); if (amount < -8f) {
                    if (apiPoints != null) { messageType = QuimiaToastType.Error; message = ApiContractGaps.SavedPoints } else savedVisible = true
                } } }
                .clickable(onClickLabel = "Abrir pontos salvos") {
                    if (apiPoints != null) { messageType = QuimiaToastType.Error; message = ApiContractGaps.SavedPoints } else savedVisible = true
                },
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp), color = tokens.secondary,
        ) {
            Box(contentAlignment = Alignment.TopCenter) {
                Box(Modifier.padding(top = 44.dp).width(90.dp).height(3.dp).background(tokens.white, CircleShape))
            }
        }
    }
    if (savedVisible) NearbySavedPointsSheet(savedIds, onDismiss = { savedVisible = false }, onSelect = {
        selectedId = it; category = sourcePoints.find { point -> point.id == it }?.category
        savedVisible = false
    }, onRemove = { id ->
        savedIds = savedIds.filterNot { it == id }; onSavedPointIdsChange(savedIds)
    })
    if (addressVisible) NearbyAddressSheet(
        initialAddress = addressFields.takeIf { it.size == 7 }?.let { NearbyAddress(it[0], it[1], it[2], it[3], it[4], it[5], it[6]) },
        onDismiss = { addressVisible = false }, onSave = {
            val draft = it
            requests.launch {
                try {
                    if (onSaveRemote != null) onSaveRemote(draft) else {
                        addressFields = listOf(draft.postalCode, draft.state, draft.city, draft.district, draft.street, draft.number, draft.complement)
                        onAddressFieldsChange(addressFields); address = draft.formatted(); onAddressChange(address)
                    }
                    addressVisible = false; messageType = QuimiaToastType.Success; message = "Endereço de referência atualizado"
                } catch (error: kotlinx.coroutines.CancellationException) { throw error }
                catch (error: Exception) { messageType = QuimiaToastType.Error; message = com.quimia.android.data.project.apiErrorMessage(error) }
            }
        },
    )
}

@Composable
internal fun NearbyPointCard(point: NearbyPoint, saved: Boolean, modifier: Modifier = Modifier, onSave: () -> Unit) {
    val tokens = quimiaColorTokens()
    Surface(modifier = modifier, shape = RoundedCornerShape(32.dp), color = tokens.surfaceBackground) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuimiaListItem(title = point.name, subtitle = point.address, contentPadding = PaddingValues(0.dp), titleMaxLines = 1, subtitleMaxLines = 1,
                containerColor = Color.Transparent, cornerRadius = 0.dp,
                trailingContent = { QuimiaShapeIcon(size = 45.dp, icon = Lucide.MapPinned, containerColor = tokens.primary) })
            QuimiaButton(modifier = Modifier.fillMaxWidth().height(43.dp), text = if (saved) "Remover dos salvos" else "Salvar",
                textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0,
                containerColor = tokens.secondary, onClick = onSave)
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Proximidade", group = "Figma", widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Proximidade · tablet paisagem", group = "Tablet QA", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Composable
private fun ProximityScreenPreview() { QuimiaTheme(false) { ProximityScreen(initialAddress = "Av Manoel Pedro Pimentel · exemplo") } }

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "GitHub · Proximidade · ponto selecionado", group = "GitHub Screens", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun ProximitySelectedPreview() {
    QuimiaTheme(false) { ProximityScreen(previewSelectedPointId = 3, initialSavedPointIds = listOf(2), initialHeaderExpanded = false) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Pontos salvos", group = "Figma", widthDp = 360, heightDp = 800)
@Composable
private fun ProximitySavedPreview() {
    QuimiaTheme(false) { ProximityScreen(initialSavedPointIds = listOf(1, 2), previewSavedPoints = true) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Alterar endereço", group = "Figma", widthDp = 393, heightDp = 852)
@Preview(name = "Endereço · fonte ampliada", group = "Acessibilidade", widthDp = 320, heightDp = 720, fontScale = 1.5f)
@Composable
private fun ProximityAddressPreview() { QuimiaTheme(false) { ProximityScreen(previewAddressForm = true) } }
