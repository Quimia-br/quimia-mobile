package com.quimia.android.presentation.preview

import androidx.compose.runtime.Composable
import com.quimia.android.presentation.screens.catalog.CatalogScreen
import com.quimia.android.presentation.screens.home.HomeScreen
import com.quimia.android.presentation.screens.proximity.ProximityScreen
import com.quimia.android.presentation.screens.shelf.ShelfScreen
import theme.QuimiaTheme

@QuimiaScreenPreviews
@Composable
private fun HomeHighContrastPreview() {
    QuimiaTheme(highContrast = true) { HomeScreen() }
}

@QuimiaScreenPreviews
@Composable
private fun CatalogHighContrastPreview() {
    QuimiaTheme(highContrast = true) { CatalogScreen() }
}

@QuimiaScreenPreviews
@Composable
private fun ShelfHighContrastPreview() {
    QuimiaTheme(highContrast = true) { ShelfScreen(savedIds = emptyList(), onSavedIdsChange = {}) }
}

@QuimiaScreenPreviews
@Composable
private fun ProximityHighContrastPreview() {
    QuimiaTheme(highContrast = true) { ProximityScreen() }
}
