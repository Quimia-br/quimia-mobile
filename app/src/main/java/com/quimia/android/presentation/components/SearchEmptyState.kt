package com.quimia.android.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import motion.quimiaContent
import states.QuimiaEmptyState
import states.QuimiaEmptyStateTheme

/** A successful search with no matches, distinct from loading and connection errors. */
@Composable
internal fun SearchEmptyState(
    title: String = "Não foi possível encontrar\no produto",
    description: String = "Por favor, tente novamente.",
    modifier: Modifier = Modifier,
) {
    QuimiaEmptyState(
        modifier = modifier.padding(top = 8.dp).quimiaContent().semantics { liveRegion = LiveRegionMode.Polite },
        icon = Lucide.TriangleAlert, iconSize = 76.dp,
        title = title, description = description,
        theme = QuimiaEmptyStateTheme.OnColor,
    )
}
