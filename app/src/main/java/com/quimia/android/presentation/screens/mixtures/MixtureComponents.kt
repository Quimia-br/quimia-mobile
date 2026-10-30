package com.quimia.android.presentation.screens.mixtures
import motion.quimiaResize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import card.QuimiaCardLayout
import com.composables.icons.lucide.*
import com.quimia.android.R
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

internal fun CatalogProduct.mixtureIngredient() = MixtureIngredient(id, name, MixtureIngredientKind.Product)

@Composable
internal fun MixtureIngredientImage(
    ingredient: MixtureIngredient, modifier: Modifier = Modifier,
    history: Boolean = false, unavailable: Boolean = false, selectedSlot: Boolean = false,
) {
    if (ingredient.kind == MixtureIngredientKind.Substance) {
        val tokens = quimiaColorTokens()
        if (history) {
            Box(modifier, contentAlignment = Alignment.Center) {
                QuimiaShapeIcon(size = 50.dp, icon = Lucide.Circle,
                    iconPainter = painterResource(R.drawable.figma_bubbles), iconSize = 24.dp,
                    containerColor = tokens.surfaceBackground, iconColor = tokens.foregroundPrimary,
                    contentDescription = ingredient.name)
            }
            return
        }
        Column(modifier.padding(vertical = if (selectedSlot) 30.dp else 0.dp), horizontalAlignment = Alignment.Start,
            verticalArrangement = if (selectedSlot) Arrangement.SpaceBetween else Arrangement.Center) {
            QuimiaShapeIcon(size = if (selectedSlot) 76.dp else 34.dp, icon = Lucide.Circle,
                iconPainter = painterResource(R.drawable.figma_bubbles), iconSize = 24.dp,
                containerColor = tokens.surfaceBackground, iconColor = tokens.foregroundPrimary)
            Text(ingredient.name, maxLines = 3, overflow = TextOverflow.Ellipsis,
                style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
        }
        return
    }
    val source = when {
        ingredient.id.startsWith("squeeze") && unavailable -> R.drawable.mixture_squeeze_unavailable_figma
        ingredient.id.startsWith("squeeze") && history -> R.drawable.mixture_history_squeeze_figma
        ingredient.id.startsWith("squeeze") -> R.drawable.mixture_squeeze_figma
        ingredient.id.startsWith("omo") && history -> R.drawable.mixture_history_omo_figma
        ingredient.id.startsWith("omo") -> R.drawable.mixture_omo_figma
        else -> sampleCatalogProducts.firstOrNull { it.id == ingredient.id }?.imageRes
    }
    if (unavailable && ingredient.id.startsWith("squeeze")) {
        Box(modifier.clipToBounds(), contentAlignment = Alignment.Center) {
            // This Figma asset includes white space to the right of the bottle.
            Image(painterResource(R.drawable.mixture_squeeze_unavailable_figma), ingredient.name,
                Modifier.requiredSize(width = 136.8.dp, height = 186.675.dp).offset(x = 27.dp),
                contentScale = ContentScale.FillBounds)
        }
        return
    }
    if (source != null) Image(painterResource(source), ingredient.name, modifier.clip(RoundedCornerShape(8.dp)),
        colorFilter = if (unavailable) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
        contentScale = if (ingredient.id.startsWith("squeeze") || ingredient.id.startsWith("omo")) ContentScale.Crop else ContentScale.Fit)
    else Box(modifier, contentAlignment = Alignment.Center) { Icon(Lucide.ImageOff, ingredient.name, tint = quimiaColorTokens().foregroundSubtle) }
}

@Composable
internal fun MixtureSlot(ingredient: MixtureIngredient?, index: Int, enabled: Boolean,
    onChoose: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier, filledHeight: Boolean = false) {
    val tokens = quimiaColorTokens()
    val tablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    val slotHeight = if (tablet) 400.dp else 351.dp
    val shape = RoundedCornerShape(24.dp)
    if (ingredient == null) {
        Surface(onClick = { if (enabled) onChoose() }, enabled = enabled,
            modifier = modifier.quimiaResize().height(if (filledHeight) slotHeight else if (tablet) 340.dp else 285.dp), shape = shape,
            color = tokens.brandSecondary.copy(alpha = 0.10f),
            border = androidx.compose.foundation.BorderStroke(2.dp, tokens.brandSecondary)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                QuimiaIconButton(icon = Lucide.Plus, size = QuimiaIconButtonSize.Large,
                    containerColor = tokens.surfaceBase, iconColor = tokens.foregroundPrimary,
                    contentDescription = "Adicionar item ${index + 1}", onClick = { if (enabled) onChoose() })
            }
        }
    } else Surface(modifier = modifier.height(slotHeight), shape = shape, color = tokens.surfaceBase, border = quimiaContrastBorder()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.CenterEnd) {
                QuimiaIconButton(icon = Lucide.X, containerColor = Color.Transparent,
                    iconColor = tokens.foregroundDisabled, size = QuimiaIconButtonSize.Small, iconSize = 24.dp,
                    contentDescription = "Remover ${ingredient.name}", onClick = { if (enabled) onRemove() })
            }
            MixtureIngredientImage(ingredient, Modifier.widthIn(max = if (tablet) 170.dp else 109.5.dp).fillMaxWidth().weight(1f), selectedSlot = true)
            QuimiaButton(text = "Alterar", modifier = Modifier.fillMaxWidth().height(43.dp), enabled = enabled,
                containerColor = tokens.secondary, textColor = tokens.foregroundPrimary, textStyle = QuimiaTextStyles.medium16,
                iconSize = 0, espacamento = 0, onClick = onChoose)
        }
    }
}

@Composable
internal fun mixtureRiskColor(risk: MixtureRisk): Color = when (risk) {
    MixtureRisk.Safe -> quimiaColorTokens().success
    MixtureRisk.Caution -> quimiaColorTokens().warning
    MixtureRisk.Dangerous -> Color(0xFFFF642B)
    MixtureRisk.Extreme -> quimiaColorTokens().error
}

@Composable
fun MixtureHistoryCard(result: MixtureResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    QuimiaCard(title = "", showTitle = false, primaryActionLabel = null, secondaryActionLabel = null,
        layout = QuimiaCardLayout.Stacked, contentPadding = 24.dp,
        modifier = modifier.clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClickLabel = "Ver mistura salva", onClick = onClick)) {
        Text(result.ingredients.joinToString(" & ") { it.name }, style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(104f).height(169.dp), contentAlignment = Alignment.Center) {
                val first = result.ingredients.getOrNull(0)
                val second = result.ingredients.getOrNull(1)
                if (second != null) MixtureIngredientImage(second,
                    Modifier.align(Alignment.Center).offset(x = 18.dp).rotate(6.13f).size(width = 56.62.dp, height = 119.63.dp), history = true)
                if (first != null) MixtureIngredientImage(first,
                    Modifier.align(Alignment.Center).offset(x = (-12).dp).rotate(-14.7f).size(width = 56.62.dp, height = 119.63.dp), history = true)
            }
            Column(Modifier.weight(150f).clip(RoundedCornerShape(32.dp)).background(tokens.surfaceBackground).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val isDanger = result.risk == MixtureRisk.Dangerous || result.risk == MixtureRisk.Extreme
                QuimiaShapeIcon(size = 50.dp,
                    icon = when { isDanger -> Lucide.Skull; result.risk == MixtureRisk.Safe -> Lucide.Shield; else -> Lucide.TriangleAlert },
                    containerColor = if (isDanger) tokens.error else mixtureRiskColor(result.risk),
                    iconColor = if (isDanger) tokens.white else tokens.foregroundPrimary)
                Text(result.risk.title, style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                Text(if (isDanger) "Não misture esses produtos." else result.risk.description,
                    style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(mixtureDateLabel(result.createdAt), style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
        if (result.demonstration) Text("Demonstração visual · sem avaliação de segurança", style = QuimiaTextStyles.medium12, color = tokens.foregroundSubtle)
    }
}
