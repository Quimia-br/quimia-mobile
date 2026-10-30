package com.quimia.android.presentation.screens.mixtures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.*
import card.QuimiaCard
import card.QuimiaCardLayout
import chip.QuimiaChip
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.system.HideSystemNavigationBar
import listItem.QuimiaListItem
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder
import theme.quimiaContrastOutline
import theme.quimiaHighContrastEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MixturePicker(
    products: List<CatalogProduct>, substances: List<MixtureIngredient>, other: MixtureIngredient?,
    current: MixtureIngredient?, onDismiss: () -> Unit, onUse: (MixtureIngredient) -> Unit,
    onOpenCatalog: () -> Unit, initialSubstances: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val content: @Composable () -> Unit = {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            MixturePickerContent(products, substances, other, current, onDismiss, onUse, onOpenCatalog,
                Modifier.fillMaxWidth().heightIn(max = maxHeight).height(if (initialSubstances) 515.dp else 650.dp), initialSubstances)
        }
    }
    if (LocalInspectionMode.current) {
        Box(Modifier.fillMaxSize().background(CatalogModalScrimColor).statusBarsPadding().padding(horizontal = 17.dp, vertical = 16.dp), contentAlignment = Alignment.BottomCenter) {
            Surface(Modifier.widthIn(max = 840.dp).fillMaxWidth(), shape = RoundedCornerShape(40.dp), color = tokens.secondary, border = quimiaContrastBorder()) { content() }
        }
    } else ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp, vertical = 16.dp), sheetMaxWidth = 840.dp,
        containerColor = tokens.secondary, scrimColor = CatalogModalScrimColor, shape = RoundedCornerShape(40.dp), dragHandle = null) {
        HideSystemNavigationBar()
        content()
    }
}

@Composable
internal fun MixturePickerContent(
    products: List<CatalogProduct>, substances: List<MixtureIngredient>, other: MixtureIngredient?, current: MixtureIngredient?,
    onDismiss: () -> Unit, onUse: (MixtureIngredient) -> Unit, onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier, initialSubstances: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    var substanceTab by rememberSaveable { mutableStateOf(initialSubstances || current?.kind == MixtureIngredientKind.Substance) }
    var chosenKey by rememberSaveable { mutableStateOf(current?.key) }
    val candidates = if (substanceTab) substances else products.map { it.mixtureIngredient() }
    val chosen = candidates.firstOrNull { it.key == chosenKey && it.key != other?.key }
    val selectionColor = if (quimiaHighContrastEnabled()) tokens.info else tokens.borderFocus
    Column(modifier.quimiaContrastOutline(RoundedCornerShape(40.dp)).padding(32.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Estante", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                Text("Estante de produtos", style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle)
            }
            QuimiaIconButton(icon = Lucide.X, onClick = onDismiss, containerColor = tokens.surfaceBase, contentDescription = "Fechar estante")
        }
        Spacer(Modifier.height(32.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
          val compact = maxWidth < 246.dp
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(false to "Produtos", true to "Substâncias").forEach { (substancesTab, label) ->
                QuimiaChip(label = label, selected = substanceTab == substancesTab,
                    modifier = Modifier.weight(1f).height(43.dp), centerContent = true,
                    contentPadding = PaddingValues(horizontal = if (compact) 4.dp else 8.dp, vertical = 12.dp),
                    labelTextStyle = if (compact) QuimiaTextStyles.medium14.copy(fontSize = 13.sp) else QuimiaTextStyles.medium15,
                    onClick = { substanceTab = substancesTab })
            }
          }
        }
        Spacer(Modifier.height(26.dp))
        if (candidates.isEmpty()) Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (substanceTab) "Sua estante ainda não tem substâncias." else "Guarde produtos na estante para selecionar uma mistura.",
                style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
            if (!substanceTab) QuimiaButton(text = "Ir para catálogo", onClick = onOpenCatalog, textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0)
        } else LazyVerticalGrid(GridCells.Adaptive(140.dp), modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(candidates, key = { it.key }) { item ->
                val unavailable = item.key == other?.key
                val selected = item.key == chosenKey && !unavailable
                QuimiaCard(title = "", showTitle = false, primaryActionLabel = null, secondaryActionLabel = null,
                    layout = QuimiaCardLayout.Stacked, contentPadding = 16.dp, outline = selected,
                    outlineColor = selectionColor, contrastOutline = !quimiaHighContrastEnabled() || chosenKey == null,
                    modifier = Modifier.height(if (substanceTab) 110.dp else 318.dp), headerContent = null) {
                    Surface(onClick = { if (!unavailable) chosenKey = item.key }, enabled = !unavailable,
                        color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                        if (substanceTab) Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(androidx.compose.ui.res.painterResource(com.quimia.android.R.drawable.figma_bubbles), null,
                                Modifier.size(24.dp), tint = tokens.foregroundDisabled)
                            Text(item.name, style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
                        } else Column(Modifier.fillMaxSize()) {
                            val product = products.first { it.id == item.id }
                            Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
                            Spacer(Modifier.height(7.dp))
                            Text(product.category, maxLines = 1, overflow = TextOverflow.Ellipsis, style = QuimiaTextStyles.medium15, color = tokens.foregroundSubtle)
                            Spacer(Modifier.height(24.dp))
                            MixtureIngredientImage(item, Modifier.fillMaxWidth().weight(1f), unavailable = unavailable)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        QuimiaButton(text = "Usar", enabled = chosen != null, modifier = Modifier.fillMaxWidth().height(52.dp),
            textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0, onClick = { chosen?.let(onUse) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MixtureResultSheet(result: MixtureResult, onClose: () -> Unit, onSave: () -> Unit, saved: Boolean) {
    val tokens = quimiaColorTokens()
    val content: @Composable () -> Unit = {
        MixtureResultContent(result, onClose, onSave, saved)
    }
    if (LocalInspectionMode.current) Box(Modifier.fillMaxSize().background(CatalogModalScrimColor).statusBarsPadding().padding(17.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth(), shape = RoundedCornerShape(40.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) { content() }
    } else ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp, vertical = 16.dp), sheetMaxWidth = 560.dp,
        containerColor = tokens.surfaceBase, scrimColor = CatalogModalScrimColor, shape = RoundedCornerShape(40.dp), dragHandle = null) {
        HideSystemNavigationBar()
        content()
    }
}

@Composable
internal fun MixtureResultContent(result: MixtureResult, onClose: () -> Unit, onSave: () -> Unit, saved: Boolean = false) {
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().quimiaContrastOutline(RoundedCornerShape(40.dp)).verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            QuimiaIconButton(icon = Lucide.X, onClick = onClose, size = QuimiaIconButtonSize.Large,
                containerColor = tokens.secondary, contentDescription = "Fechar resultado")
        }
        if (result.demonstration) {
            Spacer(Modifier.height(15.dp))
            Text("Demonstração visual · sem avaliação de segurança", style = QuimiaTextStyles.medium12, color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(26.dp))
        QuimiaShapeIcon(size = 200.dp,
            icon = when (result.risk) { MixtureRisk.Safe -> Lucide.Shield; MixtureRisk.Extreme -> Lucide.Skull; else -> Lucide.TriangleAlert },
            iconSize = 41.311.dp, containerColor = mixtureRiskColor(result.risk),
            iconColor = if (result.risk == MixtureRisk.Extreme || result.risk == MixtureRisk.Dangerous) tokens.white else tokens.foregroundPrimary)
        Spacer(Modifier.height(22.dp))
        Text(result.risk.title, style = QuimiaTextStyles.medium24,
            color = if (result.risk == MixtureRisk.Extreme) tokens.error else tokens.foregroundPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(15.dp))
        Text(result.risk.description, Modifier.padding(horizontal = 24.dp), style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle, textAlign = TextAlign.Center)
        if (result.risk != MixtureRisk.Extreme) result.ingredients.forEachIndexed { index, ingredient ->
            Spacer(Modifier.height(15.dp))
            val quantity = result.quantities.getOrNull(index)
            QuimiaListItem(title = ingredient.name, titleTextStyle = QuimiaTextStyles.medium15,
                titleMaxLines = 3, containerColor = tokens.surfaceBackground, contentPadding = PaddingValues(24.dp),
                trailingContent = if (quantity == null) null else ({
                    Surface(color = tokens.surfaceBase, shape = RoundedCornerShape(100.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Lucide.Droplet, null, Modifier.size(15.dp), tint = tokens.foregroundPrimary)
                            Text(quantity, style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary)
                        }
                    }
                }))
        }
        Spacer(Modifier.height(26.dp))
        QuimiaButton(text = if (saved) "Salvo no histórico" else "Salvar no histórico", enabled = !saved,
            containerColor = tokens.secondary, textColor = tokens.foregroundPrimary, modifier = Modifier.fillMaxWidth().height(52.dp),
            textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0, onClick = onSave)
    }
}
