package com.quimia.android.presentation.screens.catalog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import buttons.QuimiaFAB
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
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
    val bottle: Color,
    val label: Color,
)

private val sampleProducts = listOf(
    CatalogProduct("squeeze", "Limpador Cozinha Squeeze", "Limpador multiuso", Color(0xFFF15B37), Color(0xFFFFEDE0)),
    CatalogProduct("blue", "Limpador Multiuso", "Limpeza", Color(0xFF0876BD), Color.White),
    CatalogProduct("green", "Amaciante", "Roupas", Color(0xFF26B9A1), Color.White),
    CatalogProduct("squeeze-2", "Limpador Cozinha Squeeze", "Limpador multiuso", Color(0xFFF15B37), Color(0xFFFFEDE0)),
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
    var savedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
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
                .statusBarsPadding().navigationBarsPadding().padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Keep the design's top position without counting the status bar twice.
            Spacer(Modifier.height((78.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(16.dp)))
            if (bannerVisible) {
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
                        else "${selectedIds.size} produto(s) selecionado(s)",
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
                                                onSave = { savedIds = if (paired.id in savedIds) savedIds - paired.id else savedIds + paired.id },
                                                onInfo = { dialogMessage = "${paired.name} · ${paired.category}" },
                                                selected = paired.id in selectedIds,
                                                onLongSelect = { selectedIds = if (paired.id in selectedIds) selectedIds - paired.id else selectedIds + paired.id },
                                                modifier = Modifier.width(pairWidth), compact = true,
                                            )
                                        }
                                    }
                                } else {
                                    ProductCard(
                                        product, product.id in savedIds,
                                        onSave = { savedIds = if (product.id in savedIds) savedIds - product.id else savedIds + product.id },
                                        onInfo = { dialogMessage = "${product.name} · ${product.category}" },
                                        selected = product.id in selectedIds,
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
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp)
                .fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp),
        ) {
            QuimiaMenuBar(
                selectedIndex = 1, onItemSelected = onMenuItemSelected,
                fabSpacing = 24.dp,
                fab = if (showFab) ({ QuimiaFAB(onClick = {
                    if (onAiClick != null) onAiClick() else dialogMessage = "Assistente de IA ainda não está conectado."
                }, contentDescription = "Abrir assistente de IA") }) else null,
            )
        }
    }

    LabelScannerSheet(
        visible = showLabelScanner,
        onDismiss = { showLabelScanner = false },
    )

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
    onLongSelect: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean,
) {
    val tokens = quimiaColorTokens()
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp))
            .background(tokens.surfaceBase)
            .then(if (selected) Modifier.border(2.dp, tokens.primary, RoundedCornerShape(24.dp)) else Modifier)
            .combinedClickable(onClick = onInfo, onLongClick = onLongSelect)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(if (compact) 28.dp else 44.dp).background(product.bottle, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("✦", color = Color.White, style = Typography.titleSmall)
            }
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
            BottleIllustration(product.bottle, product.label, Modifier.size(if (compact) 112.dp else 148.dp))
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
private fun BottleIllustration(bodyColor: Color, labelColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * .37f, h * .24f)
            lineTo(w * .63f, h * .24f)
            lineTo(w * .76f, h * .40f)
            lineTo(w * .76f, h * .88f)
            quadraticTo(w * .76f, h * .95f, w * .69f, h * .95f)
            lineTo(w * .31f, h * .95f)
            quadraticTo(w * .24f, h * .95f, w * .24f, h * .88f)
            lineTo(w * .24f, h * .40f)
            close()
        }
        drawPath(body, bodyColor)
        drawRect(Color(0xFFDDDDD9), Offset(w * .39f, h * .17f), Size(w * .22f, h * .09f))
        drawRect(bodyColor, Offset(w * .32f, h * .12f), Size(w * .36f, h * .07f))
        drawRoundRect(labelColor, Offset(w * .28f, h * .56f), Size(w * .44f, h * .24f))
        drawCircle(bodyColor.copy(alpha = .65f), w * .12f, Offset(w * .5f, h * .68f), style = Stroke(width = 2.dp.toPx()))
        drawLine(Color.White.copy(alpha = .5f), Offset(w * .32f, h * .43f), Offset(w * .32f, h * .85f), 2.dp.toPx())
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
