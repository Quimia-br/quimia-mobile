package com.quimia.android.presentation.screens.proximity

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.quimia.android.data.auth.AuthAddress
import kotlinx.coroutines.*
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

internal fun AuthAddress.homeCoordinate(): NearbyCoordinate? = latitude?.let { lat ->
    longitude?.let { lng -> NearbyCoordinate(lat, lng).takeIf { it.isValid() } }
}
internal fun AuthAddress.homeQuery(): String? = if (rua.isNullOrBlank() || numero <= 0 || cep.filter(Char::isDigit).length != 8) null
    else listOf(rua, numero.toString(), bairro, estado, cep, "Brasil").joinToString(", ")

internal data class HomeAddressMatch(val coordinate: NearbyCoordinate, val postalCode: String?,
    val number: String?, val street: String?, val country: String?)

/** Reject CEP/city centroids and results without a matching house number. */
internal fun matchingHome(address: AuthAddress, matches: List<HomeAddressMatch>): NearbyCoordinate? = matches.firstOrNull {
    it.coordinate.isValid() && it.country.equals("BR", true) && !it.street.isNullOrBlank() &&
        it.number?.trim()?.toIntOrNull() == address.numero &&
        it.postalCode?.filter(Char::isDigit) == address.cep.filter(Char::isDigit)
}?.coordinate

internal class HomeAddressResolver(context: Context) {
    private val context = context.applicationContext
    private val cache = ConcurrentHashMap<String, NearbyCoordinate>()
    suspend fun resolve(address: AuthAddress): NearbyCoordinate? {
        address.homeCoordinate()?.let { return it }
        require(address.latitude == null && address.longitude == null) { "A API retornou coordenadas inválidas para sua casa." }
        val query = address.homeQuery() ?: return null
        cache[query]?.let { return it }
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.forLanguageTag("pt-BR"))
        val results = withTimeoutOrNull(12_000) {
            if (Build.VERSION.SDK_INT >= 33) suspendCancellableCoroutine<List<Address>> { continuation ->
                geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) { if (continuation.isActive) continuation.resume(addresses) }
                    override fun onError(errorMessage: String?) { if (continuation.isActive) continuation.resume(emptyList()) }
                })
            } else runInterruptible(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 5).orEmpty()
            }
        }.orEmpty()
        return matchingHome(address, results.filter { it.hasLatitude() && it.hasLongitude() }.map {
            HomeAddressMatch(NearbyCoordinate(it.latitude, it.longitude), it.postalCode,
                it.subThoroughfare ?: it.featureName, it.thoroughfare, it.countryCode)
        })?.also { cache[query] = it }
    }
}

internal enum class NearbyMapFocus { Initial, User, Home, Point, Manual }
internal fun nearbyCameraCoordinate(focus: NearbyMapFocus, home: NearbyCoordinate?, user: NearbyCoordinate?): NearbyCoordinate? = when (focus) {
    NearbyMapFocus.Initial -> home ?: user
    NearbyMapFocus.Home -> home
    NearbyMapFocus.User -> user
    else -> null
}?.takeIf { it.isValid() }
