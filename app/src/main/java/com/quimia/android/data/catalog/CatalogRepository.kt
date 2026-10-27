package com.quimia.android.data.catalog

import com.quimia.android.data.auth.AuthRepository
import com.quimia.android.data.auth.SessionState
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import retrofit2.http.*

@Serializable internal data class CatalogPage<T>(val itens: List<T>, val pagina: Int, val tamanho: Int,
    val totalItens: Long, val totalPaginas: Int)
@Serializable internal data class CatalogBrand(val id: Int, val nome: String, val logoUrl: String? = null)
@Serializable internal data class ApiCatalogProduct(val id: Int, val nome: String, val tipo: String,
    val descricao: String? = null, val codBarras: String? = null, val fotoUrl: String? = null, val marca: CatalogBrand? = null)
@Serializable internal data class BarcodeRequest(val codBarras: String)
@Serializable internal data class SaveCatalogRequest(val produtoIds: List<Int>)
@Serializable internal data class SavedCatalogItem(val produtoId: Int, val nome: String? = null, val status: String)
@Serializable internal data class SaveCatalogResponse(val itens: List<SavedCatalogItem>)

internal interface CatalogApi {
    @GET("api/v1/produtos") suspend fun products(@Header("Authorization") bearer: String,
        @Query("q") query: String?, @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ApiCatalogProduct>
    @GET("api/v1/produtos/{id}") suspend fun detail(@Header("Authorization") bearer: String,
        @Path("id") id: Int): ApiCatalogProduct
    @POST("api/v1/produtos/busca-por-codigo-barras") suspend fun barcode(@Header("Authorization") bearer: String,
        @Body request: BarcodeRequest): ApiCatalogProduct
    @GET("api/v1/estante/produtos") suspend fun shelf(@Header("Authorization") bearer: String,
        @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ApiCatalogProduct>
    @POST("api/v1/estante/produtos") suspend fun save(@Header("Authorization") bearer: String,
        @Body request: SaveCatalogRequest): SaveCatalogResponse
}

internal class CatalogContractException : Exception()
internal class CatalogSessionExpired : Exception()
internal interface CatalogDataSource {
    suspend fun products(userId: String, query: String, page: Int, size: Int): CatalogPage<ApiCatalogProduct>
    suspend fun savedIds(userId: String): Set<Int>
    suspend fun detail(userId: String, id: Int): ApiCatalogProduct
    suspend fun barcode(userId: String, code: String): ApiCatalogProduct
    suspend fun save(userId: String, ids: List<Int>): List<SavedCatalogItem>
}

internal class CatalogRepository(private val api: CatalogApi, private val sessions: AuthRepository) : CatalogDataSource {
    private suspend fun <T> authenticated(userId: String, block: suspend (String) -> T): T = try {
        sessions.withSession(userId, block)
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) {
        if (sessions.session.value == SessionState.SignedOut) throw CatalogSessionExpired()
        throw error
    }
    override suspend fun products(userId: String, query: String, page: Int, size: Int) = authenticated(userId) {
        api.products(it, query.trim().ifBlank { null }, page, size).also { result -> validateCatalogPage(result, page, size) }
    }
    override suspend fun savedIds(userId: String): Set<Int> = authenticated(userId) { bearer ->
        val ids = linkedSetOf<Int>()
        var page = 0
        do {
            val result = api.shelf(bearer, page, 50)
            validateCatalogPage(result, page, 50)
            ids.addAll(result.itens.map { it.id })
            page++
            if (page >= result.totalPaginas) break
            if (page >= 1000) throw CatalogContractException()
        } while (true)
        ids
    }
    override suspend fun detail(userId: String, id: Int) = authenticated(userId) { bearer ->
        api.detail(bearer, id).also { validateCatalogProduct(it); if (it.id != id) throw CatalogContractException() }
    }
    override suspend fun barcode(userId: String, code: String) = authenticated(userId) { bearer ->
        require(code.length in 1..64 && code.all { it in '0'..'9' })
        api.barcode(bearer, BarcodeRequest(code)).also {
            validateCatalogProduct(it)
            fun canonical(value: String?) = value?.let { barcode -> if (barcode.length == 12) "0$barcode" else barcode }
            if (canonical(it.codBarras) != canonical(code)) throw CatalogContractException()
        }
    }
    override suspend fun save(userId: String, ids: List<Int>): List<SavedCatalogItem> = authenticated(userId) { bearer ->
        val unique = ids.distinct()
        require(unique.size in 1..50 && unique.all { it > 0 })
        api.save(bearer, SaveCatalogRequest(unique)).itens.also { items ->
            if (items.size != unique.size || items.map { it.produtoId }.toSet() != unique.toSet() ||
                items.any { it.status !in setOf("adicionado", "ja_na_estante", "nao_encontrado") ||
                    (it.status != "nao_encontrado" && it.nome.isNullOrBlank()) }) throw CatalogContractException()
        }
    }
}

internal fun validateCatalogProduct(product: ApiCatalogProduct) {
    if (product.id <= 0 || product.nome.isBlank() || product.tipo.isBlank() ||
        product.marca?.let { it.id <= 0 || it.nome.isBlank() } == true) throw CatalogContractException()
}
internal fun validateCatalogPage(page: CatalogPage<ApiCatalogProduct>, expected: Int, size: Int) {
    if (page.pagina != expected || page.tamanho != size || page.totalItens < 0 || page.totalPaginas < 0 ||
        page.itens.size > size || page.itens.size > page.totalItens ||
        page.itens.map { it.id }.distinct().size != page.itens.size ||
        (page.totalItens == 0L && (page.totalPaginas != 0 || page.itens.isNotEmpty() || expected != 0)) ||
        (page.totalItens > 0 && (page.totalPaginas.toLong() != (page.totalItens - 1) / size + 1 ||
            expected >= page.totalPaginas || page.itens.isEmpty()))) throw CatalogContractException()
    page.itens.forEach(::validateCatalogProduct)
}
