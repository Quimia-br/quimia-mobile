package com.quimia.android.data.shelf

import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.*

@Serializable internal data class ShelfProductReference(val id: Int, val nome: String)
@Serializable internal data class ShelfApiSubstance(val id: Int, val nome: String, val casNumero: String? = null,
    val descricao: String? = null, val classes: List<String>, val produtos: List<ShelfProductReference>)
@Serializable internal data class ShelfSubstances(val itens: List<ShelfApiSubstance>)
@Serializable internal data class ShelfExposure(val rotaExposicao: String, val descricao: String? = null,
    val sintomas: String? = null, val tratamentoEspecial: String? = null, val atencaoMedicaImediata: Boolean)
@Serializable internal data class ShelfFirstAid(val produtoId: Int, val encontrada: Boolean, val porExposicao: List<ShelfExposure>,
    val orientacoesGerais: List<String>, val notasAoMedico: List<String>, val versaoFds: String? = null,
    val dataAtualizacaoFds: String? = null)
@Serializable internal data class ShelfSummary(val produtoId: Int, val resumo: String? = null, val versao: String? = null,
    val dataAtualizacao: String? = null)
@Serializable internal data class ShelfRoom(val id: Int, val nome: String, val icone: String, val totalProdutos: Int, val status: String)
@Serializable internal data class ShelfMixture(val id: Int, val produtoA: ShelfProductReference?, val produtoB: ShelfProductReference?,
    val resultado: String, val severidade: String? = null, val descricaoRisco: String? = null, val consultadoEm: String? = null)

internal interface ShelfApi {
    @GET("api/v1/estante/produtos") suspend fun products(@Header("Authorization") bearer: String,
        @Query("q") query: String?, @Query("ordem") order: String?, @Query("page") page: Int,
        @Query("size") size: Int): CatalogPage<ApiCatalogProduct>
    @GET("api/v1/estante/substancias") suspend fun substances(@Header("Authorization") bearer: String,
        @Query("q") query: String?, @Query("ordem") order: String?): ShelfSubstances
    @GET("api/v1/estante/produtos/{id}/primeiros-socorros") suspend fun firstAid(@Header("Authorization") bearer: String,
        @Path("id") id: Int): ShelfFirstAid
    @GET("api/v1/estante/produtos/{id}/resumo-simplificado") suspend fun summary(@Header("Authorization") bearer: String,
        @Path("id") id: Int): ShelfSummary
    @GET("api/v1/estante/produtos/{id}/comodo") suspend fun room(@Header("Authorization") bearer: String,
        @Path("id") id: Int): ShelfRoom
    @DELETE("api/v1/estante/produtos/{id}") suspend fun remove(@Header("Authorization") bearer: String,
        @Path("id") id: Int): Response<Unit>
    @GET("api/v1/misturas") suspend fun history(@Header("Authorization") bearer: String,
        @Query("produtoId") productId: Int, @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ShelfMixture>
}
internal interface ShelfDataSource {
    suspend fun products(user: String, query: String, order: String?, page: Int): CatalogPage<ApiCatalogProduct>
    suspend fun substances(user: String, query: String, order: String?): List<ShelfApiSubstance>
    suspend fun firstAid(user: String, id: Int): ShelfFirstAid
    suspend fun summary(user: String, id: Int): ShelfSummary
    suspend fun room(user: String, id: Int): ShelfRoom?
    suspend fun remove(user: String, id: Int)
    suspend fun history(user: String, id: Int, page: Int): CatalogPage<ShelfMixture>
}
internal class ShelfRepository(private val api: ShelfApi, private val sessions: AuthRepository) : ShelfDataSource {
    private suspend fun <T> authenticated(user: String, block: suspend (String) -> T): T = try {
        sessions.withSession(user, block)
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) {
        if (sessions.session.value == SessionState.SignedOut) throw CatalogSessionExpired()
        throw error
    }
    override suspend fun products(user: String, query: String, order: String?, page: Int) = authenticated(user) {
        api.products(it, query.trim().ifBlank { null }, order, page, 20).also { result -> validateCatalogPage(result, page, 20) }
    }
    override suspend fun substances(user: String, query: String, order: String?) = authenticated(user) {
        api.substances(it, query.trim().ifBlank { null }, order).itens.also { items ->
            if (items.map { it.id }.distinct().size != items.size || items.any { item -> item.id <= 0 || item.nome.isBlank() ||
                    item.produtos.isEmpty() || item.produtos.any { it.id <= 0 || it.nome.isBlank() } ||
                    item.produtos.map { it.id }.distinct().size != item.produtos.size }) throw CatalogContractException()
        }
    }
    override suspend fun firstAid(user: String, id: Int) = authenticated(user) {
        api.firstAid(it, id).also { aid ->
            if (aid.produtoId != id || aid.porExposicao.any { it.rotaExposicao !in setOf("inalacao", "pele", "olhos", "ingestao") } ||
                (!aid.encontrada && (aid.porExposicao.isNotEmpty() || aid.orientacoesGerais.isNotEmpty() || aid.notasAoMedico.isNotEmpty())))
                throw CatalogContractException()
        }
    }
    override suspend fun summary(user: String, id: Int) = authenticated(user) {
        api.summary(it, id).also { summary -> if (summary.produtoId != id) throw CatalogContractException() }
    }
    override suspend fun room(user: String, id: Int): ShelfRoom? = authenticated(user) { bearer ->
        try {
            api.room(bearer, id).also { room ->
                if (room.id <= 0 || room.nome.isBlank() || room.icone.isBlank() || room.totalProdutos < 1 || room.status.isBlank())
                    throw CatalogContractException()
            }
        } catch (error: HttpException) {
            val detail = runCatching { AuthApiFactory.json.parseToJsonElement(error.response()?.errorBody()?.string().orEmpty())
                .jsonObject["detail"]?.jsonPrimitive?.content }.getOrNull()
            if (error.code() == 404 && detail == "produto_sem_comodo") null else throw error
        }
    }
    override suspend fun remove(user: String, id: Int) = authenticated(user) {
        val response = api.remove(it, id)
        if (!response.isSuccessful) throw HttpException(response)
        if (response.code() != 204) throw CatalogContractException()
    }
    override suspend fun history(user: String, id: Int, page: Int) = authenticated(user) {
        api.history(it, id, page, 20).also { result ->
            if (result.pagina != page || result.tamanho != 20 || result.totalItens < 0 || result.totalPaginas < 0 ||
                result.totalPaginas.toLong() != (if (result.totalItens == 0L) 0L else (result.totalItens - 1) / 20 + 1) ||
                result.itens.size > 20 || result.itens.map { it.id }.distinct().size != result.itens.size ||
                (result.totalItens == 0L && result.itens.isNotEmpty()) ||
                (result.totalItens > 0 && (page >= result.totalPaginas || result.itens.isEmpty())) ||
                result.itens.any { item -> item.id <= 0 || item.resultado.isBlank() || item.produtoA == null || item.produtoB == null ||
                    item.produtoA.id <= 0 || item.produtoB.id <= 0 || item.produtoA.nome.isBlank() || item.produtoB.nome.isBlank() ||
                    (item.produtoA.id != id && item.produtoB.id != id) }) throw CatalogContractException()
        }
    }
}
