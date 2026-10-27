package com.quimia.android.presentation.screens.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.QuimiaColorTokens

data class CatalogProductOutline(val color: Color, val width: Dp)

fun resolveCatalogProductOutline(
    tokens: QuimiaColorTokens, highContrast: Boolean, selectionMode: Boolean, selected: Boolean,
): CatalogProductOutline? = when {
    selected -> CatalogProductOutline(if (highContrast) tokens.info else tokens.success, if (highContrast) 3.dp else 2.dp)
    highContrast && !selectionMode -> CatalogProductOutline(tokens.borderFocus, 3.dp)
    else -> null
}
