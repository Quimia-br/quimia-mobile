package com.quimia.android.presentation.screens.shelf

import com.quimia.android.presentation.screens.catalog.CatalogProduct

internal enum class ShelfSort(val label: String) {
    Alphabetical("Alfabética"), Favorites("Favoritos"), Recent("Recentes"), Oldest("Antigos"),
}

data class ShelfSubstance(val id: String, val name: String, val description: String, val productIds: List<String> = emptyList())

internal val sampleShelfSubstances = listOf(
    ShelfSubstance("bicarbonato", "Bicarbonato de sódio", "Consulte no rótulo a composição, as instruções de uso e os cuidados de armazenamento."),
    ShelfSubstance("alcool", "Álcool etílico", "Consulte no rótulo a concentração e as orientações de segurança desta substância."),
    ShelfSubstance("acido-citrico", "Ácido cítrico", "Consulte no rótulo a composição e as orientações de uso desta substância."),
)

internal fun filterShelfProducts(
    catalog: List<CatalogProduct>, savedIds: List<String>, favoriteIds: List<String>, query: String, sort: ShelfSort?,
): List<CatalogProduct> {
    val productsById = catalog.associateBy { it.id }
    val filtered = savedIds.distinct().mapNotNull(productsById::get).filter {
        it.name.contains(query.trim(), ignoreCase = true) || it.category.contains(query.trim(), ignoreCase = true)
    }
    return when (sort) {
        ShelfSort.Alphabetical -> filtered.sortedBy { it.name.lowercase() }
        ShelfSort.Favorites -> filtered.filter { it.id in favoriteIds }
        ShelfSort.Recent -> filtered.reversed()
        else -> filtered
    }
}

internal fun List<String>.toggle(id: String): List<String> = if (id in this) this - id else this + id
