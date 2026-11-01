package com.quimia.android.data.rooms

import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.*

@Serializable internal data class ApiRoomSummary(val id: Int, val nome: String, val icone: String, val totalProdutos: Int, val status: String)
@Serializable internal data class RoomConflict(val produtoIdA: Int, val produtoIdB: Int, val severidade: String?, val descricaoRisco: String?)
@Serializable internal data class RoomAssessment(val status: String, val totalProdutos: Int, val paresAvaliados: Int,
    val paresIncompativeis: Int, val severidadeMaxima: String?, val incompatibilidades: List<RoomConflict>)
@Serializable internal data class ApiRoom(val id: Int, val nome: String, val icone: String,
    val avaliacao: RoomAssessment, val produtos: List<ApiCatalogProduct>)
@Serializable internal data class CreateRoomRequest(val nome: String, val icone: String, val produtoIds: List<Int>)
@Serializable internal data class EditRoomRequest(val nome: String, val icone: String)
@Serializable internal data class AddRoomProductsRequest(val produtoIds: List<Int>)
internal class RoomRejected(val code: String, val status: Int) : Exception()

internal interface RoomsApi {
    @GET("api/v1/comodos") suspend fun list(@Header("Authorization") bearer: String, @Query("q") query: String?,
        @Query("ordem") order: String, @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ApiRoomSummary>
    @GET("api/v1/comodos/{id}") suspend fun detail(@Header("Authorization") bearer: String, @Path("id") id: Int): ApiRoom
    @POST("api/v1/comodos") suspend fun create(@Header("Authorization") bearer: String, @Body body: CreateRoomRequest): Response<ApiRoom>
    @POST("api/v1/comodos/{id}/produtos") suspend fun add(@Header("Authorization") bearer: String, @Path("id") id: Int,
        @Body body: AddRoomProductsRequest): ApiRoom
    @DELETE("api/v1/comodos/{id}/produtos/{productId}") suspend fun remove(@Header("Authorization") bearer: String,
        @Path("id") id: Int, @Path("productId") productId: Int): Response<Unit>
    @PATCH("api/v1/comodos/{id}") suspend fun edit(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body body: EditRoomRequest): ApiRoom
    @DELETE("api/v1/comodos/{id}") suspend fun delete(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<Unit>
    @GET("api/v1/estante/produtos") suspend fun shelf(@Header("Authorization") bearer: String,
        @Query("page") page: Int, @Query("size") size: Int): CatalogPage<ApiCatalogProduct>
}
internal interface RoomsDataSource {
    suspend fun list(user: String, query: String, order: String, page: Int): CatalogPage<ApiRoomSummary>
    suspend fun detail(user: String, id: Int): ApiRoom
    suspend fun shelf(user: String): List<ApiCatalogProduct>
    suspend fun create(user: String, name: String, icon: String, ids: List<Int>): ApiRoom
    suspend fun add(user: String, id: Int, ids: List<Int>): ApiRoom
    suspend fun remove(user: String, id: Int, productId: Int)
    suspend fun edit(user: String, id: Int, name: String, icon: String): ApiRoom
    suspend fun delete(user: String, id: Int)
}
internal class RoomsRepository(private val api: RoomsApi, private val sessions: AuthRepository) : RoomsDataSource {
    private suspend fun <T> authenticated(user: String, request: suspend (String) -> T): T = try {
        sessions.withSession(user, request)
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) {
        if (sessions.session.value == SessionState.SignedOut) throw CatalogSessionExpired()
        if (error is HttpException) {
            val code = runCatching { AuthApiFactory.json.parseToJsonElement(error.response()?.errorBody()?.string().orEmpty())
                .jsonObject["detail"]?.jsonPrimitive?.content }.getOrNull()
            if (code != null) throw RoomRejected(code, error.code())
        }
        throw error
    }
    override suspend fun list(user: String, query: String, order: String, page: Int) = authenticated(user) {
        api.list(it, query.trim().ifBlank { null }, order, page, 20).also { result ->
            validateRoomPage(result, page)
        }
    }
    override suspend fun detail(user: String, id: Int) = authenticated(user) {
        api.detail(it, id).also { room -> validateApiRoom(room); if (room.id != id) throw CatalogContractException() }
    }
    override suspend fun shelf(user: String): List<ApiCatalogProduct> = authenticated(user) { bearer ->
        val products = linkedMapOf<Int, ApiCatalogProduct>()
        var page = 0
        do {
            val result = api.shelf(bearer, page, 50)
            validateCatalogPage(result, page, 50)
            result.itens.forEach { products[it.id] = it }
            page++
            if (page >= result.totalPaginas) break
            if (page >= 1000) throw CatalogContractException()
        } while (true)
        products.values.toList()
    }
    override suspend fun create(user: String, name: String, icon: String, ids: List<Int>) = authenticated(user) { bearer ->
        val unique = ids.distinct()
        require(unique.size in 2..20 && unique.all { it > 0 })
        val response = api.create(bearer, CreateRoomRequest(name, icon, unique))
        if (!response.isSuccessful) throw HttpException(response)
        if (response.code() != 201) throw CatalogContractException()
        (response.body() ?: throw CatalogContractException()).also { room ->
            validateApiRoom(room)
            if (room.produtos.map { it.id }.toSet() != unique.toSet() || room.nome != normalizeRoomName(name) || room.icone != icon)
                throw CatalogContractException()
        }
    }
    override suspend fun add(user: String, id: Int, ids: List<Int>) = authenticated(user) { bearer ->
        require(ids.isNotEmpty() && ids.distinct().size <= 20 && ids.all { it > 0 })
        api.add(bearer, id, AddRoomProductsRequest(ids.distinct())).also { room ->
            validateApiRoom(room)
            if (room.id != id || !room.produtos.map { it.id }.containsAll(ids)) throw CatalogContractException()
        }
    }
    override suspend fun remove(user: String, id: Int, productId: Int) = authenticated(user) {
        val response = api.remove(it, id, productId)
        if (!response.isSuccessful) throw HttpException(response)
        if (response.code() != 204) throw CatalogContractException()
    }
    override suspend fun edit(user: String, id: Int, name: String, icon: String) = authenticated(user) { bearer ->
        val normalized = normalizeRoomName(name)
        require(normalized.isNotBlank() && normalized.length <= 100 && icon.isNotBlank())
        api.edit(bearer, id, EditRoomRequest(normalized, icon)).also { room ->
            validateApiRoom(room)
            if (room.id != id || room.nome != normalized || room.icone != icon) throw CatalogContractException()
        }
    }
    override suspend fun delete(user: String, id: Int) = authenticated(user) { bearer ->
        val response = api.delete(bearer, id)
        if (!response.isSuccessful) throw HttpException(response)
        if (response.code() != 204) throw CatalogContractException()
    }

}
internal fun normalizeRoomName(name: String) = name.trim().replace(Regex("\\s+"), " ")
internal fun validateRoomPage(page: CatalogPage<ApiRoomSummary>, expected: Int) {
    if (page.pagina != expected || page.tamanho != 20 || page.totalItens < 0 || page.totalPaginas < 0 ||
        page.totalPaginas.toLong() != (if (page.totalItens == 0L) 0L else (page.totalItens - 1) / 20 + 1) ||
        page.itens.size > 20 || page.itens.size > page.totalItens || page.itens.map { it.id }.distinct().size != page.itens.size ||
        (page.totalItens > 0 && (expected >= page.totalPaginas || page.itens.isEmpty())) ||
        page.itens.any { it.id <= 0 || it.nome.isBlank() || it.icone.isBlank() || it.totalProdutos < 0 || it.status.isBlank() })
        throw CatalogContractException()
}
internal fun validateApiRoom(room: ApiRoom) {
    val products = room.produtos.map { it.id }.toSet()
    val a = room.avaliacao
    val pairs = products.size.toLong() * (products.size - 1) / 2
    if (room.id <= 0 || room.nome.isBlank() || room.icone.isBlank() || products.size != room.produtos.size ||
        a.totalProdutos != products.size || a.status.isBlank() || a.paresAvaliados < 0 || a.paresAvaliados > pairs ||
        a.paresIncompativeis < 0 || a.paresIncompativeis > a.paresAvaliados || a.paresIncompativeis != a.incompatibilidades.size ||
        (a.status == "seguro" && (products.size < 2 || a.paresAvaliados.toLong() != pairs || a.paresIncompativeis != 0 || a.severidadeMaxima != null)) ||
        a.incompatibilidades.any { it.produtoIdA == it.produtoIdB || it.produtoIdA !in products || it.produtoIdB !in products })
        throw CatalogContractException()
    room.produtos.forEach(::validateCatalogProduct)
}
