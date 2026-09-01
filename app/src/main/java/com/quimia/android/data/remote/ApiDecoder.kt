package com.quimia.android.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

object ApiDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun <T> decodeFromString(jsonString: String, deserializer: kotlinx.serialization.DeserializationStrategy<T>): T {
        return try {
            json.decodeFromString(deserializer, jsonString)
        } catch (e: SerializationException) {
            throw ApiException("Erro ao decodificar resposta da API", e)
        } catch (e: Exception) {
            throw ApiException("Erro desconhecido ao processar resposta", e)
        }
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)


