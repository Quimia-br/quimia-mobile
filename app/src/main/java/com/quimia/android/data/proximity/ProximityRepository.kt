package com.quimia.android.data.proximity

import com.quimia.android.data.auth.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import retrofit2.http.*

@Serializable internal data class PointsPage(val itens: List<ApiPoint>, val pagina: Int, val tamanho: Int,
    val totalItens: Long, val totalPaginas: Int)
@Serializable internal data class ApiPoint(val id: Int, val nome: String, val tipo: String,
    val cep: String? = null, val estado: String? = null, val bairro: String? = null,
    val rua: String? = null, val numero: Int? = null, val complemento: String? = null) {
    val address: String get() = listOfNotNull(rua?.takeIf(String::isNotBlank), numero?.toString(),
        bairro?.takeIf(String::isNotBlank), estado?.takeIf(String::isNotBlank), cep?.takeIf(String::isNotBlank),
        complemento?.takeIf(String::isNotBlank)).joinToString(" · ").ifBlank { "Endereço não informado pela API" }
    val mapsQuery: String? get() = listOfNotNull(rua?.takeIf(String::isNotBlank), numero?.toString(),
        bairro?.takeIf(String::isNotBlank), estado?.takeIf(String::isNotBlank), cep?.takeIf(String::isNotBlank))
        .joinToString(", ").takeIf { !rua.isNullOrBlank() && (!estado.isNullOrBlank() || !cep.isNullOrBlank()) }
}
internal interface ProximityApi {
    @GET("api/v1/pontos") suspend fun points(@Header("Authorization") bearer: String,
        @Query("tipo") type: String, @Query("usarLocalizacaoCadastrada") registered: Boolean,
        @Query("page") page: Int, @Query("size") size: Int): PointsPage
    @GET("api/v1/usuarios/me/localizacao") suspend fun address(@Header("Authorization") bearer: String): AuthAddress
}
internal class ProximityContractException(message: String) : Exception(message)
internal class ProximityRepository(private val api: ProximityApi, private val sessions: AuthRepository) {
    suspend fun points(userId: String, type: String, registered: Boolean, page: Int): PointsPage {
        require(type in listOf("compra", "descarte") && page >= 0)
        return sessions.withSession(userId) { api.points(it, type, registered, page, 20) }.also { result ->
            if (result.pagina != page || result.tamanho != 20 || result.totalItens < 0 ||
                result.totalPaginas.toLong() != result.totalItens / 20 + (if (result.totalItens % 20 > 0) 1 else 0) ||
                result.itens.size.toLong() != (result.totalItens - page.toLong() * 20).coerceIn(0, 20) ||
                result.itens.map { it.id }.distinct().size != result.itens.size ||
                result.itens.any { it.id <= 0 || it.nome.isBlank() || it.tipo != type })
                throw ProximityContractException("A API retornou uma lista de pontos inconsistente.")
        }
    }
    suspend fun address(userId: String): AuthAddress? = try {
        sessions.withSession(userId) { api.address(it) }
    } catch (error: HttpException) {
        val body = error.response()?.errorBody()?.string().orEmpty()
        val detail = runCatching { AuthApiFactory.json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content }.getOrNull()
        if (error.code() == 404 && detail == "not_found") null else throw error
    }
    suspend fun saveAddress(userId: String, address: AuthAddress): AuthAddress {
        return sessions.saveInitialAddress(address, userId)
    }
}
