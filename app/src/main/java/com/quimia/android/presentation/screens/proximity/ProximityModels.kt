package com.quimia.android.presentation.screens.proximity

internal enum class NearbyCategory(val label: String) { Collection("Descarte"), Shopping("Compra") }

internal val nearbyStates = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")

internal data class NearbyPoint(
    val id: Int, val name: String, val address: String, val category: NearbyCategory,
    val distance: String, val x: Float, val y: Float,
    val coordinate: NearbyCoordinate,
)

internal data class NearbyCoordinate(val latitude: Double, val longitude: Double) {
    fun isValid(): Boolean = latitude.isFinite() && longitude.isFinite() &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0
}

internal val nearbyDefaultCoordinate = NearbyCoordinate(-23.5505, -46.6333)

internal enum class NearbyLocationStatus { Idle, Loading, Ready, PermissionDenied, PermissionBlocked, Disabled, Unavailable }

internal fun nearbyLocationPermission(fine: Boolean, coarse: Boolean): Boolean = fine || coarse

internal fun nearbyLocationProviders(fine: Boolean, enabled: Set<String>): List<String> =
    listOfNotNull("fused".takeIf { it in enabled }, "gps".takeIf { fine && it in enabled }, "network".takeIf { it in enabled })
internal fun nearbyLocationProvider(fine: Boolean, enabled: Set<String>): String? = nearbyLocationProviders(fine, enabled).firstOrNull()
internal fun nearbyFixIsFresh(ageMillis: Long): Boolean = ageMillis in 0..120_000L
internal fun nearbyLocationZoom(precise: Boolean, accuracyMeters: Float): Float = when {
    accuracyMeters >= 5_000f -> 10.5f
    !precise || accuracyMeters >= 1_000f -> 12f
    accuracyMeters >= 200f -> 14f
    else -> 15.5f
}

internal val nearbyPoints = listOf(
    NearbyPoint(1, "Ecoponto · exemplo", "Rua Exemplo, 100 · São Paulo", NearbyCategory.Collection, "650 m", .253f, .245f, NearbyCoordinate(-23.5485, -46.6383)),
    NearbyPoint(2, "Cooperativa · exemplo", "Avenida Exemplo, 200 · São Paulo", NearbyCategory.Collection, "1,2 km", .797f, .228f, NearbyCoordinate(-23.5475, -46.6263)),
    NearbyPoint(3, "Ponto de coleta · exemplo", "Rua Exemplo, 300 · São Paulo", NearbyCategory.Collection, "1,8 km", .183f, .425f, NearbyCoordinate(-23.5545, -46.6393)),
    NearbyPoint(4, "Loja · exemplo", "Avenida Exemplo, 400 · São Paulo", NearbyCategory.Shopping, "900 m", .62f, .22f, NearbyCoordinate(-23.5495, -46.6293)),
)

internal fun filterNearbyPoints(points: List<NearbyPoint>, category: NearbyCategory?, query: String = ""): List<NearbyPoint> =
    points.filter { (category == null || it.category == category) &&
        (it.name.contains(query.trim(), true) || it.address.contains(query.trim(), true)) }

internal fun toggleSavedPoint(ids: List<Int>, id: Int): List<Int> =
    if (id in ids) ids.filterNot { it == id } else (ids + id).distinct()

data class NearbyAddress(
    val postalCode: String, val state: String, val city: String, val district: String,
    val street: String, val number: String, val complement: String,
) {
    fun validationError(): String? = when {
        postalCode.filter(Char::isDigit).length != 8 -> "Informe um CEP com 8 dígitos."
        state.uppercase() !in nearbyStates -> "Selecione uma UF válida."
        city.isBlank() || district.isBlank() || number.isBlank() -> "Preencha cidade, bairro e número."
        else -> null
    }
    fun formatted(): String = (if (street.isBlank()) "$district, $number · $city/${state.uppercase()}" else "$street, $number · $district · $city/${state.uppercase()}") +
        if (complement.isBlank()) "" else " · $complement"
}
