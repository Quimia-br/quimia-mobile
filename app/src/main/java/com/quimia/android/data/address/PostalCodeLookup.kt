package com.quimia.android.data.address

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class PostalAddress(val postalCode: String, val state: String, val city: String, val district: String, val street: String) {
    val complete: Boolean get() = state in PostalStates && city.isNotBlank() && district.isNotBlank() && street.isNotBlank()
}

sealed interface PostalLookupResult {
    data class Found(val address: PostalAddress) : PostalLookupResult
    data object NotFound : PostalLookupResult
    data object Unavailable : PostalLookupResult
    data object InvalidResponse : PostalLookupResult
}

private val PostalStates = setOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")
private val PostalJson = Json { ignoreUnknownKeys = true }

fun normalizedPostalCode(value: String): String = value.filter { it in '0'..'9' }.take(8)

@Serializable
private data class ViaCepResponse(val cep: String = "", val uf: String = "", val localidade: String = "",
    val bairro: String = "", val logradouro: String = "", val erro: JsonPrimitive? = null)

internal fun decodePostalAddress(json: String, requestedCode: String): PostalLookupResult = runCatching {
    val value = PostalJson.decodeFromString<ViaCepResponse>(json)
    if (value.erro?.booleanOrNull == true) PostalLookupResult.NotFound
    else if (value.erro != null && value.erro.booleanOrNull == null) PostalLookupResult.InvalidResponse
    else if (normalizedPostalCode(value.cep) != requestedCode || value.uf !in PostalStates || value.localidade.isBlank()) PostalLookupResult.InvalidResponse
    else PostalLookupResult.Found(PostalAddress(requestedCode, value.uf, value.localidade.trim(), value.bairro.trim(), value.logradouro.trim()))
}.getOrDefault(PostalLookupResult.InvalidResponse)

object ViaCepLookup {
    private val client by lazy { OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS).callTimeout(12, TimeUnit.SECONDS).build() }

    suspend fun lookup(postalCode: String): PostalLookupResult {
        val code = normalizedPostalCode(postalCode)
        if (code.length != 8) return PostalLookupResult.NotFound
        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(Request.Builder().url("https://viacep.com.br/ws/$code/json/").build())
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resume(PostalLookupResult.Unavailable)
                }
                override fun onResponse(call: Call, response: Response) {
                    val result = response.use { runCatching {
                        if (!it.isSuccessful) PostalLookupResult.Unavailable
                        else it.body?.string()?.let { body -> decodePostalAddress(body, code) } ?: PostalLookupResult.InvalidResponse
                    }.getOrDefault(PostalLookupResult.Unavailable) }
                    if (continuation.isActive) continuation.resume(result)
                }
            })
        }
    }
}
