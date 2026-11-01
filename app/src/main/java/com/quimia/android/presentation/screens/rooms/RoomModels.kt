package com.quimia.android.presentation.screens.rooms

import androidx.compose.runtime.saveable.Saver
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class RoomIcon(val label: String) {
    Mirror("Espelho"), Refrigerator("Cozinha"), Fence("Área externa"), Laundry("Lavanderia"),
    DoubleBed("Quarto de casal"), SingleBed("Quarto"), Toilet("Banheiro"), Sofa("Sala de estar"),
    Pool("Piscina"), Shelving("Estante"),
}

@Serializable
enum class RoomSafety { NotEvaluated, Critical, Safe }

enum class RoomPage { List, Create, Detail, Adding, Editing, Saving, Success, Error }

@Serializable
data class QuimiaRoom(
    val id: String,
    val name: String,
    val icon: RoomIcon,
    val productIds: List<String>,
    val safety: RoomSafety = RoomSafety.NotEvaluated,
    val knownIcon: Boolean = true,
)

internal enum class RoomSort(val label: String, val apiValue: String) {
    Alphabetical("Alfabética", "alfabetica"), Recent("Recentes", "recentes"), Oldest("Antigos", "antigos")
}

internal fun roomGridColumns(availableWidthDp: Float): Int = when {
    availableWidthDp >= 700f -> 4
    availableWidthDp >= 500f -> 3
    else -> 2
}

internal fun roomIconColumns(availableWidthDp: Float): Int =
    ((availableWidthDp + 8f) / 64f).toInt().coerceIn(1, 5)

internal fun roomStartPage(productId: String?, roomId: String?, fallback: RoomPage = RoomPage.List): RoomPage = when {
    productId != null -> RoomPage.Create
    roomId != null && fallback == RoomPage.List -> RoomPage.Detail
    else -> fallback
}

internal fun roomsForProduct(rooms: List<QuimiaRoom>, productId: String): List<QuimiaRoom> =
    rooms.filter { productId in it.productIds }.distinctBy { it.id }

val RoomListSaver = Saver<List<QuimiaRoom>, String>(
    save = { Json.encodeToString(it) },
    restore = { runCatching { Json.decodeFromString<List<QuimiaRoom>>(it) }.getOrDefault(emptyList()) },
)

internal fun roomValidationError(name: String, icon: RoomIcon?, selectedIds: List<String>, availableIds: List<String>): String? = when {
    name.isBlank() -> "Informe o nome do cômodo."
    name.trim().length > 60 -> "Use até 60 caracteres no nome."
    icon == null -> "Escolha um ícone para o cômodo."
    selectedIds.none { it in availableIds } -> "Selecione pelo menos um produto da sua estante."
    selectedIds.any { it !in availableIds } -> "A lista de produtos mudou. Confira sua seleção."
    else -> null
}

internal fun filterRooms(rooms: List<QuimiaRoom>, query: String, sort: RoomSort): List<QuimiaRoom> {
    val filtered = rooms.filter { it.name.contains(query.trim(), ignoreCase = true) }
    return when (sort) {
        RoomSort.Alphabetical -> filtered.sortedBy { it.name.lowercase() }
        RoomSort.Recent -> filtered.reversed()
        RoomSort.Oldest -> filtered
    }
}

internal fun roomProducts(room: QuimiaRoom, catalog: List<CatalogProduct>): List<CatalogProduct> {
    val indexed = catalog.associateBy { it.id }
    return room.productIds.distinct().mapNotNull(indexed::get)
}

internal fun QuimiaRoom.withoutProduct(id: String) = copy(
    productIds = productIds.filterNot { it == id }, safety = RoomSafety.NotEvaluated,
)

internal fun List<QuimiaRoom>.upsertRoom(room: QuimiaRoom): List<QuimiaRoom> =
    if (any { it.id == room.id }) map { if (it.id == room.id) room else it } else this + room

internal val previewRooms = listOf(
    QuimiaRoom("bathroom", "Banheiro", RoomIcon.Toilet, listOf("squeeze", "squeeze-2", "squeeze-3", "squeeze-4"), RoomSafety.Critical),
    QuimiaRoom("bedroom", "Quarto", RoomIcon.DoubleBed, listOf("squeeze")),
    QuimiaRoom("kitchen", "Cozinha", RoomIcon.Refrigerator, listOf("squeeze", "squeeze-2", "squeeze-3", "squeeze-4"), RoomSafety.Safe),
    QuimiaRoom("living", "Sala de estar", RoomIcon.Sofa, listOf("squeeze")),
)
