package com.quimia.android.presentation.screens.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import buttons.QuimiaIconButton
import card.QuimiaCard
import checkBox.QuimiaCheckBox
import com.composables.icons.lucide.*
import com.quimia.android.R
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import image.QuimiaImage
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

internal val RoomIcon.vector: ImageVector get() = when (this) {
    RoomIcon.Refrigerator -> Lucide.Refrigerator
    RoomIcon.Fence -> Lucide.Fence
    RoomIcon.Laundry -> Lucide.WashingMachine
    RoomIcon.DoubleBed -> Lucide.BedDouble
    RoomIcon.SingleBed -> Lucide.BedSingle
    RoomIcon.Toilet -> Lucide.Bath
    RoomIcon.Sofa -> Lucide.Armchair
    else -> Lucide.Circle
}

internal val RoomIcon.drawable: Int? get() = when (this) {
    RoomIcon.Mirror -> R.drawable.room_mirror
    RoomIcon.Toilet -> R.drawable.room_toilet
    RoomIcon.Pool -> R.drawable.room_pool
    RoomIcon.Shelving -> R.drawable.room_shelving
    else -> null
}

@Composable
internal fun RoomShapeIcon(icon: RoomIcon, size: Dp, modifier: Modifier = Modifier, critical: Boolean = false) {
    val tokens = quimiaColorTokens()
    QuimiaShapeIcon(size = size, modifier = modifier, icon = icon.vector,
        iconPainter = icon.drawable?.takeUnless { icon == RoomIcon.Toilet }?.let { painterResource(it) }, iconSize = 24.dp,
        containerColor = if (critical) tokens.error else tokens.surfaceBackground,
        iconColor = if (critical) tokens.white else tokens.foregroundPrimary)
}

@Composable
internal fun RoomProductCard(
    product: CatalogProduct, modifier: Modifier = Modifier,
    selectable: Boolean = false, checked: Boolean = false, critical: Boolean = false,
    previewQuantity: Boolean = false, onCheckedChange: (Boolean) -> Unit = {}, onActions: () -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    val height = if (selectable) 297.dp else if (previewQuantity) 318.dp else 256.dp
    QuimiaCard(
        title = product.name, primaryActionLabel = null, secondaryActionLabel = null,
        showTitle = false, showDescription = false, endIcon = null, sectionSpacing = 0.dp,
        modifier = modifier.height(height).then(if (selectable) Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Selecionar ${product.name}"
        }.toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange) else Modifier),
        outline = selectable && checked, outlineWidth = 2.dp, contentPadding = 16.dp,
        containerColor = if (critical) tokens.secondary else tokens.surfaceBase,
    ) {
        Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (selectable) QuimiaAvatar(size = 45.dp, photo = painterResource(product.logoRes), contentDescription = null)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(product.name, style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(product.category, style = QuimiaTextStyles.medium15, color = tokens.foregroundSubtle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.fillMaxWidth().height(if (selectable) 144.dp else 155.dp), contentAlignment = Alignment.Center) {
                QuimiaImage(painter = painterResource(if (critical && product.id.startsWith("squeeze")) R.drawable.room_product_critical_figma else product.imageRes),
                    contentDescription = product.name, modifier = Modifier.requiredSize(131.dp),
                    backgroundColor = if (critical) tokens.secondary else tokens.surfaceBase,
                    contentScale = ContentScale.Crop)
                if (!selectable) QuimiaIconButton(modifier = Modifier.align(Alignment.BottomEnd), icon = Lucide.Menu,
                    containerColor = if (critical) tokens.surfaceBase else tokens.secondary,
                    contentDescription = "Ações de ${product.name}", onClick = onActions)
            }
            if (selectable) Row(Modifier.fillMaxWidth().height(34.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                QuimiaCheckBox(checked = checked, onCheckedChange = null)
            }
            if (previewQuantity) Column {
                Text("Quantidade recomendada:", style = QuimiaTextStyles.medium12, color = tokens.foregroundSubtle)
                Text("15 ml", style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
            }
        }
    }
}
