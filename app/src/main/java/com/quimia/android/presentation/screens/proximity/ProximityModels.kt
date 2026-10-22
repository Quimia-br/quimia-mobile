package com.quimia.android.presentation.screens.proximity

internal enum class NearbyCategory(val label: String) { Collection("Descarte"), Shopping("Compras") }

internal data class NearbyPoint(
    val id: Int, val name: String, val address: String, val category: NearbyCategory,
    val distance: String, val x: Float, val y: Float,
)

internal val nearbyPoints = listOf(
    NearbyPoint(1, "Ecoponto · exemplo", "Rua Exemplo, 100 · São Paulo", NearbyCategory.Collection, "650 m", .18f, .25f),
    NearbyPoint(2, "Cooperativa · exemplo", "Avenida Exemplo, 200 · São Paulo", NearbyCategory.Collection, "1,2 km", .65f, .43f),
    NearbyPoint(3, "Ponto de coleta · exemplo", "Rua Exemplo, 300 · São Paulo", NearbyCategory.Collection, "1,8 km", .4f, .68f),
    NearbyPoint(4, "Loja · exemplo", "Avenida Exemplo, 400 · São Paulo", NearbyCategory.Shopping, "900 m", .62f, .22f),
)

internal fun filterNearbyPoints(points: List<NearbyPoint>, category: NearbyCategory?, query: String = ""): List<NearbyPoint> =
    points.filter { (category == null || it.category == category) &&
        (it.name.contains(query.trim(), true) || it.address.contains(query.trim(), true)) }

internal fun toggleSavedPoint(ids: List<Int>, id: Int): List<Int> =
    if (id in ids) ids.filterNot { it == id } else (ids + id).distinct()

internal data class NearbyAddress(
    val postalCode: String, val state: String, val city: String, val district: String,
    val street: String, val number: String, val complement: String,
) {
    fun validationError(): String? = when {
        postalCode.filter(Char::isDigit).length != 8 -> "Informe um CEP com 8 dígitos."
        !state.uppercase().matches(Regex("[A-Z]{2}")) -> "Informe a sigla da UF com 2 letras."
        city.isBlank() || district.isBlank() || street.isBlank() || number.isBlank() -> "Preencha cidade, bairro, logradouro e número."
        else -> null
    }
    fun formatted(): String = "$street, $number · $district · $city/${state.uppercase()}" +
        if (complement.isBlank()) "" else " · $complement"
}
