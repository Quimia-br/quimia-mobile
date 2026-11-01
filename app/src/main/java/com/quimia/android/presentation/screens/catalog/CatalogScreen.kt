package com.quimia.android.presentation.screens.catalog

import theme.QuimiaTextStyles

import motion.quimiaContent
import com.quimia.android.presentation.components.SearchEmptyState
import motion.QuimiaExpand
import motion.QuimiaReveal
import motion.QuimiaStateContent
import motion.QuimiaMotion
import motion.quimiaPress
import motion.quimiaResize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import theme.quimiaSkeleton
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import buttons.QuimiaButton
import buttons.QuimiaFAB
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.X
import header.QuimiaSearchBar
import com.quimia.android.presentation.components.rememberVoiceInput
import image.QuimiaImage
import menuBar.QuimiaMenuBar
import menuBar.QuimiaMenuBarBackdrop
import menuBar.QuimiaMenuBarBottomSpacing
import menuBar.QuimiaMenuBarHeight
import com.quimia.android.presentation.system.HideSystemNavigationBar
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTheme
import theme.RadiusLarge
import theme.Typography
import theme.quimiaColorTokens
import theme.quimiaHighContrastEnabled
import theme.quimiaContrastBorder
import theme.quimiaContrastOutline
import title.QuimiaSectionTitle

internal val CatalogModalScrimColor = Color(0xFF808080).copy(alpha = 0.16f)

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    onMenuItemSelected: (Int) -> Unit = {},
    onAiClick: (() -> Unit)? = null,
    showFab: Boolean = true,
    previewItemLimit: Int = Int.MAX_VALUE,
    initialSavedProductIds: List<String> = emptyList(),
    onSavedProductIdsChange: (List<String>) -> Unit = {},
    initialSelectedProductIds: List<String> = emptyList(),
    initialBannerVisible: Boolean = true,
    initialQuery: String = "",
    products: List<CatalogProduct>? = null,
    onQueryChange: ((String) -> Unit)? = null,
    onSaveProducts: ((List<String>) -> Unit)? = null,
    onProductDetails: ((CatalogProduct) -> Unit)? = null,
    saving: Boolean = false,
    canSave: Boolean = true,
    saveMessage: String? = null,
    onDismissSaveMessage: () -> Unit = {},
    statusContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    onBarcode: ((String) -> Unit)? = null,
    saveMessageSuccess: Boolean = true,
    showEmpty: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val focusManager = LocalFocusManager.current
    val playSound = feedback.LocalQuimiaSound.current
    var query by rememberSaveable(initialQuery) { mutableStateOf(initialQuery) }
    fun changeQuery(value: String) { query = value.take(100); onQueryChange?.invoke(query) }
    val voiceInput = rememberVoiceInput { changeQuery(it) }
    var bannerVisible by rememberSaveable { mutableStateOf(initialBannerVisible) }
    var showLabelScanner by rememberSaveable { mutableStateOf(false) }
    var savedIds by remember(initialSavedProductIds) { mutableStateOf(initialSavedProductIds.toSet()) }
    var selectedIds by remember { mutableStateOf(initialSelectedProductIds.toSet()) }
    var detailProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(3_000)
            successMessage = null
        }
    }
    val filtered = remember(products, query, previewItemLimit, savedIds) {
        val matches = products ?: sampleCatalogProducts.take(previewItemLimit).filter {
            it.name.contains(query.trim(), ignoreCase = true) || it.category.contains(query.trim(), ignoreCase = true)
        }
        matches.sortedBy { it.id in savedIds }
    }
    LaunchedEffect(savedIds) { if (onSaveProducts != null) selectedIds = selectedIds - savedIds }
    var selectionQuery by remember { mutableStateOf(query) }
    LaunchedEffect(query) { if (selectionQuery != query) { selectedIds = emptySet(); selectionQuery = query } }
    val bannerMessage = saveMessage ?: successMessage
    val showBanner = bannerMessage != null || (bannerVisible && selectedIds.isEmpty() && filtered.isNotEmpty())
    fun save(ids: List<String>) {
        if (onSaveProducts != null) onSaveProducts(ids)
        else {
            savedIds = savedIds + ids
            onSavedProductIdsChange(savedIds.toList())
            successMessage = "Produto(s) adicionado(s) com sucesso"
            selectedIds = emptySet()
        }
    }
    fun showDetails(product: CatalogProduct) {
        if (onProductDetails != null) onProductDetails(product) else detailProduct = product
    }
    fun select(id: String) {
        if (saving || id in savedIds) return
        if (id in selectedIds) { selectedIds = selectedIds - id; playSound(feedback.QuimiaSound.Selection) }
        else if (selectedIds.size < 50) { selectedIds = selectedIds + id; playSound(feedback.QuimiaSound.Selection) }
        else dialogMessage = "Selecione até 50 produtos por vez."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.surfaceBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(276.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tokens.brandSecondary.copy(alpha = 0.86f),
                            tokens.surfaceBackground.copy(alpha = 0.72f),
                            tokens.surfaceBackground,
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxWidth().fillMaxHeight()
                .statusBarsPadding().navigationBarsPadding().padding(bottom = QuimiaMenuBarHeight + QuimiaMenuBarBottomSpacing + 27.dp)
                .verticalScroll(rememberScrollState()).quimiaContent()
        ) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            QuimiaExpand(showBanner) {
                CatalogBanner(
                    message = bannerMessage ?: "Clique e segure nos\nprodutos para selecionar",
                    success = bannerMessage != null && saveMessageSuccess,
                    onDismiss = {
                        if (saveMessage != null) onDismissSaveMessage() else if (successMessage != null) successMessage = null else bannerVisible = false
                    },
                )
            }
            QuimiaSectionTitle(
                title = "Catálogo de produtos", titleSize = 25.sp,
                textStyle = Typography.headlineSmall.copy(lineHeight = 30.sp),
                horizontalPadding = 32.dp, verticalPadding = 32.dp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuimiaSearchBar(
                    value = query, onValueChange = { changeQuery(it) }, modifier = Modifier.weight(1f).height(60.dp),
                    placeholder = "Pesquisar", icon = Lucide.Mic,
                    iconDescription = "Pesquisar por voz", onClick = voiceInput,
                    horizontalPadding = 16.dp, verticalPadding = 12.dp,
                    spacedBy = 12.dp, textStyle = Typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 20.sp),
                    searchIconSize = 22.dp,
                )
                QuimiaIconButton(
                    modifier = Modifier.size(58.dp),
                    icon = Lucide.Camera,
                    size = QuimiaIconButtonSize.Large,
                    iconSize = 22.dp,
                    containerColor = tokens.surfaceBase,
                    iconColor = tokens.textPrimary,
                    contentDescription = "Buscar produto pela câmera",
                    onClick = {
                        focusManager.clearFocus()
                        showLabelScanner = true
                    },
                )
            }
            statusContent?.invoke()
            if (showEmpty && filtered.isEmpty()) {
                SearchEmptyState()
            } else {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(24.dp)) {
                    val gap = 16.dp
                    val layoutWidth = maxWidth
                    val pairWidth = if (layoutWidth >= 240.dp) (layoutWidth - gap) / 2 else layoutWidth
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        filtered.forEachIndexed { index, product ->
                            if (index == 0 || index % 2 == 1 || layoutWidth < 240.dp) {
                                if (index > 0 && layoutWidth >= 240.dp) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                        filtered.drop(index).take(2).forEach { paired ->
                                            ProductCard(
                                                paired, paired.id in savedIds,
                                                onSave = { save(listOf(paired.id)) },
                                                onInfo = { showDetails(paired) },
                                                saving = saving, canSave = canSave,
                                                selected = paired.id in selectedIds,
                                                selectionMode = selectedIds.isNotEmpty(),
                                                onLongSelect = { select(paired.id) },
                                                modifier = Modifier.width(pairWidth), compact = true,
                                            )
                                        }
                                    }
                                } else {
                                    ProductCard(
                                        product, product.id in savedIds,
                                        onSave = { save(listOf(product.id)) },
                                        onInfo = { showDetails(product) },
                                        saving = saving, canSave = canSave,
                                        selected = product.id in selectedIds,
                                        selectionMode = selectedIds.isNotEmpty(),
                                        onLongSelect = { select(product.id) },
                                        modifier = Modifier.fillMaxWidth(), compact = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            footerContent?.invoke()
            Spacer(Modifier.height(24.dp))
        }

        QuimiaMenuBarBackdrop(
            modifier = Modifier.align(Alignment.BottomCenter),
            barHeight = if (selectedIds.isEmpty()) QuimiaMenuBarHeight else 124.dp,
        )

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp)
                .fillMaxWidth().navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = QuimiaMenuBarBottomSpacing),
        ) {
            if (selectedIds.isEmpty()) {
                QuimiaMenuBar(
                    selectedIndex = 1, onItemSelected = onMenuItemSelected,
                    fabSpacing = 24.dp,
                    fab = if (showFab) ({ QuimiaFAB(modifier = Modifier.padding(end = 5.dp), onClick = {
                        if (onAiClick != null) onAiClick() else dialogMessage = "Assistente de IA ainda não está conectado."
                    }, contentDescription = "Abrir assistente de IA") }) else null,
                )
            } else {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(56.dp)).background(tokens.surfaceBase)
                        .quimiaContrastOutline(RoundedCornerShape(56.dp)).padding(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuimiaIconButton(
                        modifier = Modifier.size(48.dp), icon = Lucide.X,
                        contentDescription = "Cancelar seleção", size = QuimiaIconButtonSize.Medium,
                        containerColor = tokens.secondary, iconColor = tokens.textPrimary,
                        onClick = { if (!saving) selectedIds = emptySet() },
                    )
                    QuimiaButton(
                        modifier = Modifier.weight(1f).height(48.dp),
                        text = if (saving) "Guardando…" else "Guardar todos (${selectedIds.size})",
                        enabled = canSave && !saving,
                        textStyle = Typography.labelLarge, containerColor = tokens.primary,
                        textColor = tokens.foregroundPrimary,
                        iconSize = 0, espacamento = 0,
                        onClick = { save(selectedIds.toList()) },
                    )
                }
            }
        }
    }

    LabelScannerSheet(
        visible = showLabelScanner,
        onDismiss = { showLabelScanner = false },
        onBarcode = onBarcode,
    )

    detailProduct?.let { product ->
        CatalogProductDetailsSheet(
            product = product,
            saved = product.id in savedIds,
            onDismiss = { detailProduct = null },
            onSave = {
                val wasSaved = product.id in savedIds
                val nextSavedIds = if (wasSaved) savedIds - product.id else savedIds + product.id
                savedIds = nextSavedIds
                onSavedProductIdsChange(nextSavedIds.toList())
                if (!wasSaved) successMessage = "Produto adicionado com sucesso"
                detailProduct = null
            },
        )
    }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Catálogo") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { dialogMessage = null }) { Text("Entendi") } },
        )
    }
}

@Composable
private fun CatalogBanner(message: String, success: Boolean, onDismiss: () -> Unit) {
    toast.QuimiaToast(message = message, type = if (success) toast.QuimiaToastType.Success else toast.QuimiaToastType.Info,
        modifier = Modifier.padding(16.dp), onDismiss = onDismiss)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductCard(
    product: CatalogProduct,
    saved: Boolean,
    onSave: () -> Unit,
    onInfo: () -> Unit,
    selected: Boolean,
    selectionMode: Boolean,
    onLongSelect: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean,
    saving: Boolean = false,
    canSave: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val cardShape = RoundedCornerShape(RadiusLarge.dp)
    val outline = resolveCatalogProductOutline(tokens, quimiaHighContrastEnabled(), selectionMode, selected)
        ?: if (saved && !selectionMode) CatalogProductOutline(tokens.success, 2.dp) else null
    val interactions = remember { MutableInteractionSource() }
    val playSound = feedback.LocalQuimiaSound.current
    val pressed by interactions.collectIsPressedAsState()
    QuimiaCard(
        modifier = modifier
            .quimiaPress(pressed, QuimiaMotion.CardPressScale)
            .clip(cardShape)
            .combinedClickable(
                interactionSource = interactions, indication = androidx.compose.foundation.LocalIndication.current,
                onClick = { if (selectionMode) onLongSelect() else { playSound(feedback.QuimiaSound.Tap); onInfo() } },
                onLongClick = onLongSelect,
            ),
        title = product.name,
        description = product.category,
        primaryActionLabel = null,
        secondaryActionLabel = null,
        showTitle = false,
        showDescription = false,
        endIcon = null,
        contentPadding = 0.dp,
        sectionSpacing = 0.dp,
        contrastOutline = false,
        outline = outline != null,
        outlineColor = outline?.color,
        outlineWidth = outline?.width ?: 3.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CatalogImage(product, logo = true, modifier = Modifier.size(48.dp))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        product.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = Typography.bodyLarge,
                        color = tokens.textPrimary,
                    )
                    Text(product.category, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = Typography.labelLarge, color = tokens.textSecondary)
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(if (compact) 230.dp else 202.dp),
                contentAlignment = Alignment.Center,
            ) {
                CatalogImage(product, modifier = Modifier.size(width = if (compact) 122.dp else 154.dp, height = 154.dp))
                QuimiaIconButton(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp).size(34.dp),
                    icon = Lucide.Info,
                    size = QuimiaIconButtonSize.Small,
                    iconSize = 18.dp,
                    containerColor = tokens.secondary,
                    iconColor = tokens.foregroundPrimary,
                    contentDescription = "Informações sobre ${product.name}",
                    onClick = onInfo,
                )
            }
            CatalogSaveAction(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                saved = saved, saving = saving, canSave = canSave, onSave = onSave,
            )
        }
    }
}

@Composable
private fun CatalogSaveAction(
    saved: Boolean, saving: Boolean, canSave: Boolean, onSave: () -> Unit,
    modifier: Modifier = Modifier, detail: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    QuimiaStateContent(target = saved, modifier = modifier) { isSaved ->
    if (isSaved && !detail) {
        Box(Modifier.fillMaxSize().quimiaSkeleton(8.dp).semantics { stateDescription = "Produto já adicionado à sua estante" }, contentAlignment = Alignment.Center) {
            Text("Esse produto já está na sua estante", style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
        }
    } else if (isSaved) {
        Surface(
            modifier = Modifier.fillMaxSize().quimiaSkeleton(1000.dp).semantics { stateDescription = "Produto já adicionado à sua estante" },
            shape = CircleShape,
            color = tokens.success.copy(alpha = 0.12f).compositeOver(tokens.surfaceBase),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.success.copy(alpha = if (quimiaHighContrastEnabled()) 1f else 0.4f)),
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = tokens.textPrimary)
                Text(if (detail) "Na sua estante" else "Na estante", modifier = Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = if (detail) Typography.bodyLarge else Typography.labelLarge, color = tokens.textPrimary)
            }
        }
    } else QuimiaButton(
        modifier = Modifier.fillMaxSize(),
        text = if (saving) "Guardando…" else "Guardar", textStyle = Typography.bodyLarge,
        containerColor = if (detail) tokens.primary else tokens.secondary,
        textColor = tokens.foregroundPrimary,
        iconSize = 0, espacamento = 0, enabled = canSave && !saving && !saved, onClick = onSave,
    )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogProductDetailsSheet(
    product: CatalogProduct,
    saved: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saving: Boolean = false,
    canSave: Boolean = true,
    statusContent: (@Composable () -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp),
        sheetMaxWidth = 560.dp,
        shape = RoundedCornerShape(36.dp),
        containerColor = tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        scrimColor = CatalogModalScrimColor,
        dragHandle = null,
    ) {
        CatalogProductDetailsContent(
            product = product,
            saved = saved,
            onDismiss = onDismiss,
            onSave = onSave,
            modifier = Modifier.fillMaxWidth(), saving = saving, canSave = canSave, statusContent = statusContent,
        )
    }
}

@Composable
private fun CatalogProductDetailsContent(
    product: CatalogProduct,
    saved: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
    canSave: Boolean = true,
    statusContent: (@Composable () -> Unit)? = null,
) {
    HideSystemNavigationBar()
    val tokens = quimiaColorTokens()
    val clipboard = LocalClipboardManager.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(36.dp),
        color = tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        border = quimiaContrastBorder(),
    ) {
        Column(Modifier.fillMaxWidth().padding(32.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = Typography.headlineSmall, color = tokens.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(product.category, style = Typography.labelLarge, color = tokens.textSecondary)
                }
                QuimiaIconButton(
                    icon = Lucide.X,
                    onClick = onDismiss,
                    contentDescription = "Fechar detalhes",
                    size = QuimiaIconButtonSize.Medium,
                    containerColor = tokens.secondary,
                    iconColor = tokens.textPrimary,
                )
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).quimiaContent()) {
                CatalogImage(product, detail = true, modifier = Modifier.fillMaxWidth().height(212.dp).padding(vertical = 16.dp))
                CatalogDescriptionCard(description = product.description)
                if (product.barcode.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    QuimiaListItem(
                        title = "Código de barras",
                        subtitle = product.barcode,
                        contentPadding = PaddingValues(24.dp),
                        trailingContent = {
                            QuimiaIconButton(
                                icon = Lucide.Copy,
                                iconSize = 20.dp,
                                onClick = { clipboard.setText(AnnotatedString(product.barcode)) },
                                contentDescription = "Copiar código de barras",
                                containerColor = tokens.surfaceBase,
                            )
                        },
                    )
                }
            }
            statusContent?.invoke()
            Spacer(Modifier.height(16.dp))
            CatalogSaveAction(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                saved = saved, saving = saving, canSave = canSave, onSave = onSave, detail = true,
            )
        }
    }
}

@Composable
internal fun CatalogDescriptionCard(description: String) {
    val tokens = quimiaColorTokens()
    var expanded by rememberSaveable(description) { mutableStateOf(false) }
    var canExpand by remember(description) { mutableStateOf(false) }
    QuimiaCard(
        title = "Descrição",
        modifier = Modifier.quimiaResize(),
        primaryActionLabel = null,
        secondaryActionLabel = null,
        endIcon = null,
        containerColor = tokens.surfaceBackground,
        contentPadding = 24.dp,
        sectionSpacing = 8.dp,
        headerContent = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Descrição",
                    modifier = Modifier.weight(1f),
                    style = Typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 20.sp),
                    color = tokens.textPrimary,
                )
                if (canExpand) {
                    QuimiaIconButton(
                        icon = Lucide.ChevronDown, iconRotationDegrees = if (expanded) 180f else 0f,
                        size = QuimiaIconButtonSize.Small,
                        containerColor = tokens.surfaceBase,
                        contentDescription = if (expanded) "Recolher descrição" else "Expandir descrição completa",
                        onClick = { expanded = !expanded },
                    )
                }
            }
        },
    ) {
        Text(
            description,
            style = Typography.labelLarge,
            color = tokens.textSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                val overflow = if (expanded) result.lineCount > 5 else result.hasVisualOverflow
                if (canExpand != overflow) canExpand = overflow
            },
        )
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Catálogo · 360 × 805", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun CatalogSinglePreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen(previewItemLimit = 1) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Catálogo lista · 360 × 805", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Preview(name = "Catálogo lista · 360 × 800", widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Catálogo compacto · 320 × 640", widthDp = 320, heightDp = 640, showSystemUi = true)
@Preview(name = "Catálogo celular grande · 420 × 911", widthDp = 420, heightDp = 911, showSystemUi = true)
@Preview(name = "Catálogo tablet", widthDp = 800, heightDp = 1340, showSystemUi = true)
@Preview(name = "Catálogo tablet paisagem", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Preview(name = "GitHub · Catálogo preenchido · 360 × 900", group = "GitHub Screens", widthDp = 360, heightDp = 900, showSystemUi = true)
@Composable
private fun CatalogListPreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen(initialBannerVisible = false) }
}

@Preview(name = "Catálogo · pesquisa sem resultados", group = "Estados", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun CatalogSearchEmptyPreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen(initialQuery = "Produto inexistente") }
}

@Preview(name = "Catálogo · produtos guardados · claro", widthDp = 360, heightDp = 805, showSystemUi = true)
@Preview(name = "Catálogo · produtos guardados · escuro", widthDp = 360, heightDp = 805, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CatalogSavedProductsPreview() {
    QuimiaTheme {
        CatalogScreen(initialBannerVisible = false, initialSavedProductIds = sampleCatalogProducts.take(2).map { it.id })
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Catálogo seleção · 360 × 805", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun CatalogSelectionPreview() {
    QuimiaTheme(darkTheme = false) {
        CatalogScreen(initialSelectedProductIds = sampleCatalogProducts.map { it.id })
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Catálogo detalhes · 360 × 805", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun CatalogDetailsPreview() {
    CatalogDetailsPreviewContent(sampleCatalogProducts.first())
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Descrição longa · fonte ampliada", group = "Acessibilidade", widthDp = 360, heightDp = 805, fontScale = 1.5f, showSystemUi = true)
@Composable
private fun CatalogLongDescriptionPreview() {
    val product = sampleCatalogProducts.first()
    CatalogDetailsPreviewContent(product.copy(description = List(3) { product.description }.joinToString("\n\n")))
}

@Composable
private fun CatalogDetailsPreviewContent(product: CatalogProduct) {
    QuimiaTheme(darkTheme = false) {
        Box(Modifier.fillMaxSize()) {
            CatalogScreen(previewItemLimit = 3, initialBannerVisible = false)
            Box(Modifier.fillMaxSize().background(CatalogModalScrimColor))
            Box(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                CatalogProductDetailsContent(
                    product = product, saved = false,
                    onDismiss = {}, onSave = {},
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                )
            }
        }
    }
}
