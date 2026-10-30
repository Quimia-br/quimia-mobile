package com.quimia.android.data.mixtures

import com.quimia.android.data.auth.AuthRepository
import com.quimia.android.data.catalog.ApiCatalogProduct
import com.quimia.android.data.catalog.CatalogPage
import com.quimia.android.data.catalog.validateCatalogPage
import com.quimia.android.data.home.HomeMixture
import com.quimia.android.data.home.HomePage
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

@Serializable internal data class AnalyzeMixtureRequest(val produtoIdA: Int, val produtoIdB: Int)

internal interface MixturesApi {
    @GET("api/v1/estante/produtos") suspend fun shelf(@Header("Authorization") bearer: String,
        @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ApiCatalogProduct>
    @POST("api/v1/misturas") suspend fun analyze(@Header("Authorization") bearer: String,
        @Body request: AnalyzeMixtureRequest): Response<HomeMixture>
    @GET("api/v1/misturas") suspend fun history(@Header("Authorization") bearer: String,
        @Query("page") page: Int, @Query("size") size: Int, @Query("produtoId") productId: Int?): HomePage<HomeMixture>
}

internal class MixturesContractException(message: String) : Exception(message)

internal class MixturesRepository(private val api: MixturesApi, private val sessions: AuthRepository) {
    suspend fun shelf(userId: String): List<ApiCatalogProduct> = sessions.withSession(userId) { bearer ->
        val products = mutableListOf<ApiCatalogProduct>()
        var page = 0
        do {
            val result = api.shelf(bearer, page, 50)
            validateCatalogPage(result, page, 50)
            products.addAll(result.itens)
            page++
            if (page >= result.totalPaginas) break
            if (page >= 1000) throw MixturesContractException("A estante retornou páginas demais.")
        } while (true)
        products.distinctBy { it.id }
    }

    suspend fun analyze(userId: String, first: Int, second: Int): HomeMixture {
        require(first > 0 && second > 0 && first != second)
        return sessions.withSession(userId) { bearer ->
            val response = api.analyze(bearer, AnalyzeMixtureRequest(first, second))
            if (!response.isSuccessful) throw retrofit2.HttpException(response)
            val result = response.body()
            if (response.code() != 201 || result == null) throw MixturesContractException("A API não confirmou a análise da mistura.")
            validateMixture(result)
            if (setOf(result.produtoA?.id, result.produtoB?.id) != setOf(first, second))
                throw MixturesContractException("A análise retornada pertence a outros produtos.")
            result
        }
    }

    suspend fun history(userId: String, page: Int = 0, productId: Int? = null): HomePage<HomeMixture> = sessions.withSession(userId) { bearer ->
        val result = api.history(bearer, page, 20, productId)
        if (result.pagina != page || result.tamanho != 20 || result.totalItens < 0 || result.itens.size > 20 ||
            result.totalPaginas.toLong() != (result.totalItens / 20 + if (result.totalItens % 20 > 0) 1 else 0) ||
            result.itens.map { it.id }.distinct().size != result.itens.size)
            throw MixturesContractException("O histórico retornou uma página inconsistente.")
        result.itens.forEach(::validateMixture)
        result
    }
}

internal fun validateMixture(result: HomeMixture) {
    if (result.id <= 0 || result.produtoA == null || result.produtoB == null ||
        result.produtoA.id <= 0 || result.produtoB.id <= 0 || result.produtoA.id == result.produtoB.id ||
        result.produtoA.nome.isBlank() || result.produtoB.nome.isBlank() ||
        result.resultado !in setOf("compativel", "incompativel", "nao_avaliado") ||
        (result.resultado == "incompativel" && result.severidade !in setOf("baixa", "media", "alta", "critica")) ||
        (result.resultado != "incompativel" && !result.severidade.isNullOrBlank()))
        throw MixturesContractException("A API retornou uma avaliação incompleta ou incompatível.")
}
