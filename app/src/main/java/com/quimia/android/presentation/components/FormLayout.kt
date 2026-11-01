package com.quimia.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.quimiaColorTokens

@Composable
internal fun formKeyboardVisible(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0

@Composable
internal fun formTopSpace(normal: Dp): Dp = if (formKeyboardVisible()) 16.dp
    else (normal - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)

/** The scrolling form ends before this opaque, measured footer; it never floats over an input. */
@Composable
internal fun FormFooter(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp,
    bottomPadding: Dp = 24.dp,
    spacing: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bottom = if (formKeyboardVisible()) minOf(bottomPadding, 12.dp) else bottomPadding
    Surface(modifier.fillMaxWidth(), color = quimiaColorTokens().surfaceBackground) {
        Column(Modifier.fillMaxWidth().padding(start = horizontalPadding, end = horizontalPadding, top = 16.dp, bottom = bottom),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}
