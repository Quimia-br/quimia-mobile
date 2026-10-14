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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.annotation.DrawableRes
import com.quimia.android.R
import kotlinx.coroutines.delay
import buttons.QuimiaButton
import buttons.QuimiaFAB
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.X
import header.QuimiaSearchBar
import menuBar.QuimiaMenuBar
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import title.QuimiaSectionTitle

private data class CatalogProduct(
    val id: String,
    val name: String,
    val category: String,
    @param:DrawableRes val imageRes: Int,
    @param:DrawableRes val logoRes: Int,
    @param:DrawableRes val detailImageRes: Int = imageRes,
    val description: String = "Descrição não disponível para este produto.",
    val barcode: String = "",
)

private val mrMusculoProduct = CatalogProduct(
        "squeeze", "Limpador Cozinha Squeeze", "Limpador multiuso",
        R.drawable.catalog_mr_musculo, R.drawable.catalog_logo_mr_musculo,
        detailImageRes = R.drawable.catalog_mr_musculo_detail,
        description = "O Mr Músculo Tira Limo contém uma espuma para acabar com manchas de mofo e umidade que penetra e remove as sujeiras mais difíceis do banheiro, para você limpar menos e viver mais.",
        barcode = "4784784837498",
    )

private val sampleProducts = listOf(
    mrMusculoProduct,
    CatalogProduct("veja", "Limpador Multiuso Veja", "Limpador multiuso", R.drawable.catalog_veja, R.drawable.catalog_logo_veja),
    CatalogProduct("omo", "Lava-Roupas Líquido Omo", "Lava-roupas", R.drawable.catalog_omo, R.drawable.catalog_logo_omo),
    mrMusculoProduct.copy(id = "squeeze-2"),
    CatalogProduct("omo-2", "Lava-Roupas Líquido Omo", "Lava-roupas", R.drawable.catalog_omo, R.drawable.catalog_logo_omo),
)

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    onMenuItemSelected: (Int) -> Unit = {},
    onAiClick: (() -> Unit)? = null,
    showFab: Boolean = true,
    previewItemLimit: Int = Int.MAX_VALUE,
) {
    val tokens = quimiaColorTokens()
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var bannerVisible by rememberSaveable { mutableStateOf(true) }
    var showLabelScanner by rememberSaveable { mutableStateOf(false) }
    var savedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
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
        sampleProducts.take(previewItemLimit).filter {
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
                .statusBarsPadding().navigationBarsPadding()
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
                        Text("✓", color = tokens.onForeground, style = Typography.titleMedium)
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
                                                    if (paired.id in savedIds) savedIds = savedIds - paired.id
                                                    else {
                                                        savedIds = savedIds + paired.id
                                                        successMessage = "Produto adicionado com sucesso"
                                                    }
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
                                            if (product.id in savedIds) savedIds = savedIds - product.id
                                            else {
                                                savedIds = savedIds + product.id
                                                successMessage = "Produto adicionado com sucesso"
                                            }
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
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.height(100.dp))
        }

        if (!showLabelScanner) Box(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp)
                .fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp),
        ) {
            if (selectedIds.isEmpty()) {
                QuimiaMenuBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    selectedIndex = 1, onItemSelected = onMenuItemSelected,
                    fabSpacing = 24.dp,
                    fab = if (showFab) ({ QuimiaFAB(onClick = {
                        if (onAiClick != null) onAiClick() else dialogMessage = "Assistente de IA ainda não está conectado."
                    }, contentDescription = "Abrir assistente de IA") }) else null,
                )
            } else {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(32.dp)).background(tokens.surfaceBase)
                        .padding(8.dp),
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
                            val count = selectedIds.size
                            savedIds = (savedIds + selectedIds).distinct()
                            selectedIds = emptySet()
                            successMessage = if (count == 1) "Produto adicionado com sucesso" else "$count produtos adicionados com sucesso"
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
                savedIds = savedIds + product.id
                detailProduct = null
                successMessage = "${product.name} adicionado com sucesso"
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
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp))
            .background(tokens.surfaceBase)
            .then(if (selected) Modifier.border(2.dp, tokens.success, RoundedCornerShape(24.dp)) else Modifier)
            .combinedClickable(
                onClick = { if (selectionMode) onLongSelect() else onInfo() },
                onLongClick = onLongSelect,
            )
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(product.logoRes),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 28.dp else 44.dp).clip(CircleShape),
                contentScale = ContentScale.Fit,
            )
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    product.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = (if (compact) Typography.bodySmall else Typography.bodyMedium).copy(fontWeight = FontWeight.Normal),
                    color = tokens.textPrimary,
                )
                Text(product.category, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = if (compact) Typography.bodySmall else Typography.bodyMedium, color = tokens.textSecondary)
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(if (compact) 150.dp else 170.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(product.imageRes),
                contentDescription = product.name,
                modifier = Modifier.size(if (compact) 112.dp else 148.dp),
                contentScale = ContentScale.Fit,
            )
            Box(Modifier.align(Alignment.BottomEnd).size(44.dp).background(tokens.secondary, CircleShape),
                contentAlignment = Alignment.Center) {
                IconButton(onClick = onInfo, modifier = Modifier.size(44.dp)) {
                    Icon(Lucide.Info, contentDescription = "Informações sobre ${product.name}", modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        QuimiaButton(
            modifier = Modifier.fillMaxWidth().height(43.dp),
            text = if (saved) "Guardado" else "Guardar",
            textStyle = Typography.bodyMedium,
            containerColor = tokens.primary, textColor = tokens.textPrimary,
            iconSize = 0, espacamento = 0, onClick = onSave,
        )
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
            Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 26.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = 747.dp).fillMaxHeight(),
                shape = RoundedCornerShape(40.dp), color = tokens.surfaceBase,
                contentColor = tokens.textPrimary, shadowElevation = 16.dp,
            ) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(product.name, style = Typography.titleMedium.copy(fontSize = 24.sp), color = tokens.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(product.category, style = Typography.bodySmall, color = tokens.textSecondary)
                        }
                        QuimiaIconButton(
                            icon = Lucide.X, onClick = onDismiss, contentDescription = "Fechar detalhes",
                            size = QuimiaIconButtonSize.Medium, containerColor = tokens.secondary,
                            iconColor = tokens.textPrimary,
                        )
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(product.detailImageRes),
                                contentDescription = product.name,
                                modifier = Modifier.width(147.dp).height(172.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                .background(tokens.surfaceBackground).padding(24.dp),
                        ) {
                            Text("Descrição", style = Typography.bodyMedium, color = tokens.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(product.description, style = Typography.bodySmall, color = tokens.textSecondary)
                        }
                        Spacer(Modifier.height(16.dp))
                        if (product.barcode.isNotBlank()) {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                    .background(tokens.surfaceBackground).padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Código de barras", style = Typography.bodyMedium, color = tokens.textPrimary)
                                    Spacer(Modifier.height(6.dp))
                                    Text(product.barcode, style = Typography.bodySmall, color = tokens.textSecondary)
                                }
                                IconButton(onClick = { clipboard.setText(AnnotatedString(product.barcode)) }) {
                                    Icon(Lucide.Copy, contentDescription = "Copiar código de barras", tint = tokens.textPrimary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    QuimiaButton(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        text = if (saved) "Guardado" else "Guardar", textStyle = Typography.bodyMedium,
                        containerColor = tokens.primary, iconSize = 0, espacamento = 0,
                        onClick = onSave,
                    )
                }
            }
        }
    }
}

@Preview(name = "Catálogo referência · 360 × 800", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun CatalogSinglePreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen(previewItemLimit = 1) }
}

@Preview(name = "Catálogo lista · 360 × 800", widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Catálogo compacto · 320 × 640", widthDp = 320, heightDp = 640, showSystemUi = true)
@Preview(name = "Catálogo tablet", widthDp = 800, heightDp = 1340, showSystemUi = true)
@Composable
private fun CatalogListPreview() {
    QuimiaTheme(darkTheme = false) { CatalogScreen() }
}
