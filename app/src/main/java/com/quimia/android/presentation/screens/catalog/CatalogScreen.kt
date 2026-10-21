package com.quimia.android.presentation.screens.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import buttons.QuimiaButton
import buttons.QuimiaFAB
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.X
import com.quimia.android.R
import header.QuimiaSearchBar
import image.QuimiaImage
import menuBar.QuimiaMenuBar
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import title.QuimiaSectionTitle

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    onMenuItemSelected: (Int) -> Unit = {},
    onAiClick: (() -> Unit)? = null,
    showFab: Boolean = true,
    previewItemLimit: Int = Int.MAX_VALUE,
    initialSavedProductIds: List<String> = emptyList(),
    onSavedProductIdsChange: (List<String>) -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var bannerVisible by rememberSaveable { mutableStateOf(true) }
    var showLabelScanner by rememberSaveable { mutableStateOf(false) }
    var savedIds by remember(initialSavedProductIds) { mutableStateOf(initialSavedProductIds.toSet()) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var detailProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(3_000)
            successMessage = null
        }
    }
    val filtered = remember(query, previewItemLimit) {
        sampleCatalogProducts.take(previewItemLimit).filter {
            it.name.contains(query.trim(), ignoreCase = true) || it.category.contains(query.trim(), ignoreCase = true)
        }
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
                .statusBarsPadding().navigationBarsPadding().padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Keep the design's top position without counting the status bar twice.
            Spacer(Modifier.height((78.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(16.dp)))
            if (successMessage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(40.dp)).background(tokens.surfaceBase).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp).background(tokens.success, CircleShape), contentAlignment = Alignment.Center) {
                        Text("✓", color = tokens.textPrimary, style = Typography.titleMedium)
                    }
                    Text(successMessage.orEmpty(), Modifier.weight(1f).padding(horizontal = 12.dp), style = Typography.bodySmall, color = tokens.textPrimary)
                    IconButton(onClick = { successMessage = null }, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, contentDescription = "Fechar confirmação", modifier = Modifier.size(20.dp))
                    }
                }
            } else if (bannerVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(40.dp)).background(tokens.surfaceBase)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp).background(tokens.secondary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Info, contentDescription = null, modifier = Modifier.size(22.dp), tint = tokens.textPrimary)
                    }
                    Text(
                        if (selectedIds.isEmpty()) "Clique e segure para\nselecionar produtos"
                        else if (selectedIds.size == 1) "1 produto selecionado"
                        else "${selectedIds.size} produtos selecionados",
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Normal, lineHeight = 18.sp),
                        color = tokens.textPrimary,
                    )
                    IconButton(onClick = { bannerVisible = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, contentDescription = "Fechar dica", modifier = Modifier.size(22.dp))
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
            QuimiaSectionTitle(
                title = "Catálogo de produtos", titleSize = 24.sp,
                horizontalPadding = 32.dp, verticalPadding = 4.dp,
            )
            Spacer(Modifier.height(36.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuimiaSearchBar(
                    value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f).height(56.dp),
                    placeholder = "Pesquisar",icon = Lucide.Mic,
                    horizontalPadding = 16.dp, verticalPadding = 12.dp,
                    spacedBy = 12.dp, textStyle = Typography.bodyMedium,
                )
                QuimiaIconButton(
                    modifier = Modifier.size(56.dp),
                    icon = Lucide.Camera,
                    size = QuimiaIconButtonSize.Large,
                    iconSize = 20.dp,
                    containerColor = tokens.surfaceBase,
                    iconColor = tokens.textPrimary,
                    contentDescription = "Buscar produto pela câmera",
                    onClick = {
                        focusManager.clearFocus()
                        showLabelScanner = true
                    },
                )
            }
            Spacer(Modifier.height(36.dp))

            if (filtered.isEmpty()) {
                Text(
                    "Nenhum produto encontrado",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    style = Typography.bodyMedium, color = tokens.textSecondary,
                )
            } else {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    val gap = 12.dp
                    val layoutWidth = maxWidth
                    val pairWidth = if (layoutWidth >= 280.dp) (layoutWidth - gap) / 2 else layoutWidth
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        filtered.forEachIndexed { index, product ->
                            if (index != 2 || filtered.size < 3 || layoutWidth < 280.dp) {
                                if (index == 1 && filtered.size >= 3 && layoutWidth >= 280.dp) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                        filtered.slice(1..2).forEach { paired ->
                                            ProductCard(
                                                paired, paired.id in savedIds,
                                                onSave = {
                                                    val wasSaved = paired.id in savedIds
                                                    val nextSavedIds = if (wasSaved) savedIds - paired.id else savedIds + paired.id
                                                    savedIds = nextSavedIds
                                                    onSavedProductIdsChange(nextSavedIds.toList())
                                                    if (!wasSaved) successMessage = "Produto adicionado com sucesso"
                                                },
                                                onInfo = { detailProduct = paired },
                                                selected = paired.id in selectedIds,
                                                selectionMode = selectedIds.isNotEmpty(),
                                                onLongSelect = { selectedIds = if (paired.id in selectedIds) selectedIds - paired.id else selectedIds + paired.id },
                                                modifier = Modifier.width(pairWidth), compact = true,
                                            )
                                        }
                                    }
                                } else {
                                    ProductCard(
                                        product, product.id in savedIds,
                                        onSave = {
                                            val wasSaved = product.id in savedIds
                                            val nextSavedIds = if (wasSaved) savedIds - product.id else savedIds + product.id
                                            savedIds = nextSavedIds
                                            onSavedProductIdsChange(nextSavedIds.toList())
                                            if (!wasSaved) successMessage = "Produto adicionado com sucesso"
                                        },
                                        onInfo = { detailProduct = product },
                                        selected = product.id in selectedIds,
                                        selectionMode = selectedIds.isNotEmpty(),
                                        onLongSelect = { selectedIds = if (product.id in selectedIds) selectedIds - product.id else selectedIds + product.id },
                                        modifier = Modifier.fillMaxWidth(), compact = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(124.dp))
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp)
                .fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp),
        ) {
            if (selectedIds.isEmpty()) {
                QuimiaMenuBar(
                    selectedIndex = 1, onItemSelected = onMenuItemSelected,
                    fabSpacing = 24.dp,
                    fab = if (showFab) ({ QuimiaFAB(onClick = {
                        if (onAiClick != null) onAiClick() else dialogMessage = "Assistente de IA ainda não está conectado."
                    }, contentDescription = "Abrir assistente de IA") }) else null,
                )
            } else {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(32.dp)).background(tokens.surfaceBase).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuimiaIconButton(
                        modifier = Modifier.size(48.dp), icon = Lucide.X,
                        contentDescription = "Cancelar seleção", size = QuimiaIconButtonSize.Medium,
                        containerColor = tokens.secondary, iconColor = tokens.textPrimary,
                        onClick = { selectedIds = emptySet() },
                    )
                    QuimiaButton(
                        modifier = Modifier.widthIn(min = 150.dp).height(48.dp),
                        text = "Guardar todos (${selectedIds.size})",
                        textStyle = Typography.bodyMedium, containerColor = tokens.primary,
                        iconSize = 0, espacamento = 0,
                        onClick = {
                            savedIds = savedIds + selectedIds
                            onSavedProductIdsChange(savedIds.toList())
                            successMessage = "Produtos adicionados com sucesso"
                            selectedIds = emptySet()
                        },
                    )
                }
            }
        }
    }

    LabelScannerSheet(
        visible = showLabelScanner,
        onDismiss = { showLabelScanner = false },
    )

    detailProduct?.let { product ->
        CatalogProductDetailsDialog(
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
) {
    val tokens = quimiaColorTokens()
    QuimiaCard(
        modifier = modifier
            .then(if (selected) Modifier.border(2.dp, tokens.success, RoundedCornerShape(24.dp)) else Modifier)
            .combinedClickable(
                onClick = { if (selectionMode) onLongSelect() else onInfo() },
                onLongClick = onLongSelect,
            ),
        title = product.name,
        description = product.category,
        primaryActionLabel = null,
        secondaryActionLabel = null,
        showTitle = false,
        showDescription = false,
        contentPadding = 0.dp,
        sectionSpacing = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuimiaImage(
                    painter = painterResource(product.logoRes),
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 30.dp else 42.dp),
                    shape = CircleShape,
                    backgroundColor = tokens.surfaceBackground,
                )
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    product.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = (if (compact) Typography.bodySmall else Typography.bodyMedium).copy(fontWeight = FontWeight.Medium),
                    color = tokens.textPrimary,
                )
                Text(product.category, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = if (compact) Typography.bodySmall else Typography.bodyMedium, color = tokens.textSecondary)
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(if (compact) 148.dp else 174.dp),
                contentAlignment = Alignment.Center,
            ) {
                QuimiaImage(
                    painter = painterResource(product.imageRes),
                    contentDescription = product.name,
                    modifier = Modifier.size(if (compact) 116.dp else 146.dp),
                    contentScale = ContentScale.Fit,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = tokens.surfaceBase,
                )
                Box(Modifier.align(Alignment.BottomEnd).size(40.dp).background(tokens.secondary, CircleShape),
                    contentAlignment = Alignment.Center) {
                    IconButton(onClick = onInfo, modifier = Modifier.size(40.dp)) {
                        Icon(Lucide.Info, contentDescription = "Informações sobre ${product.name}", modifier = Modifier.size(20.dp))
                    }
                }
            }
            if (selected) {
                Text("Selecionado", style = Typography.bodySmall, color = tokens.success,
                    modifier = Modifier.padding(bottom = 8.dp))
            }
            QuimiaButton(
                modifier = Modifier.fillMaxWidth().height(43.dp),
                text = if (saved) "Guardado" else "Guardar",
                textStyle = Typography.bodyMedium,
                containerColor = tokens.primary, textColor = tokens.onPrimary,
                iconSize = 0, espacamento = 0, onClick = onSave,
            )
        }
    }
}

@Composable
private fun CatalogProductDetailsDialog(
    product: CatalogProduct,
    saved: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    val clipboard = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = 740.dp).fillMaxHeight(),
                shape = RoundedCornerShape(36.dp), color = tokens.surfaceBase,
                contentColor = tokens.textPrimary, shadowElevation = 16.dp,
            ) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name, style = Typography.titleMedium, color = tokens.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(product.category, style = Typography.bodySmall, color = tokens.textSecondary)
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
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        QuimiaImage(
                            painter = painterResource(product.detailImageRes),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxWidth().height(224.dp).padding(18.dp),
                            contentScale = ContentScale.Fit,
                            backgroundColor = tokens.surfaceBase,
                        )
                        QuimiaCard(
                            title = "Descrição",
                            description = product.description,
                            primaryActionLabel = null,
                            secondaryActionLabel = null,
                            contentPadding = 20.dp,
                            sectionSpacing = 8.dp,
                            titleTextStyle = Typography.bodyMedium,
                            descriptionTextStyle = Typography.bodySmall,
                        )
                        if (product.barcode.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            QuimiaCard(
                                title = "Código de barras",
                                description = product.barcode,
                                icon = Lucide.Info,
                                primaryActionLabel = null,
                                secondaryActionLabel = null,
                                contentPadding = 18.dp,
                                sectionSpacing = 0.dp,
                                titleTextStyle = Typography.bodyMedium,
                                descriptionTextStyle = Typography.bodySmall,
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    QuimiaIconButton(
                                        icon = Lucide.Copy,
                                        onClick = { clipboard.setText(AnnotatedString(product.barcode)) },
                                        contentDescription = "Copiar código de barras",
                                        containerColor = tokens.secondary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    QuimiaButton(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        text = if (saved) "Guardado" else "Guardar",
                        textStyle = Typography.bodyMedium,
                        containerColor = tokens.primary,
                        textColor = tokens.onPrimary,
                        iconSize = 0,
                        espacamento = 0,
                        onClick = onSave,
                    )
                }
            }
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun CatalogSinglePreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen(previewItemLimit = 1) }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Catálogo lista · 360 × 800", widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Catálogo compacto · 320 × 640", widthDp = 320, heightDp = 640, showSystemUi = true)
@Preview(name = "Catálogo celular grande · 420 × 911", widthDp = 420, heightDp = 911, showSystemUi = true)
@Preview(name = "Catálogo tablet", widthDp = 800, heightDp = 1340, showSystemUi = true)
@Preview(name = "Catálogo tablet paisagem", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Preview(name = "GitHub · Catálogo preenchido · 360 × 900", group = "GitHub Screens", widthDp = 360, heightDp = 900, showSystemUi = true)
@Composable
private fun CatalogListPreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen() }
}
