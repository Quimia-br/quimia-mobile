package com.quimia.android.data.project

import kotlinx.serialization.json.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ProjectApi {
    @POST("api/v1/auth/mobile/login") suspend fun login(@Body body: JsonObject): ApiSession
    @POST("api/v1/auth/firebase") suspend fun firebase(@Body body: JsonObject): ApiSession
    @POST("api/v1/auth/register") suspend fun register(@Body body: JsonObject): ApiUser
    @POST("api/v1/auth/mobile/refresh") fun refresh(@Body body: JsonObject): Call<ApiSession>
    @POST("api/v1/auth/forgot-password") suspend fun forgot(@Body body: JsonObject): ApiRecoveryChallenge
    @POST("api/v1/auth/verify-recovery-code") suspend fun verifyCode(@Body body: JsonObject): ApiRecoveryGrant
    @POST("api/v1/auth/reset-password") suspend fun reset(@Body body: JsonObject)
    @GET("api/v1/usuarios/me") suspend fun me(): ApiUser
    @PATCH("api/v1/usuarios/me") suspend fun updateProfile(@Body body: JsonObject): ApiUser
    @GET("api/v1/usuarios/me/localizacao") suspend fun address(): ApiAddress
    @PUT("api/v1/usuarios/me/localizacao") suspend fun updateAddress(@Body body: ApiAddress): ApiAddress
    @GET("api/v1/produtos") suspend fun products(@Query("page") page: Int, @Query("size") size: Int = 50, @Query("q") query: String? = null): ApiPage<ApiProduct>
    @GET("api/v1/produtos/{id}") suspend fun product(@Path("id") id: Int): ApiProduct
    @POST("api/v1/produtos/busca-por-codigo-barras") suspend fun barcode(@Body body: JsonObject): ApiProduct
    @GET("api/v1/estante/produtos") suspend fun shelf(@Query("page") page: Int, @Query("size") size: Int = 50): ApiPage<ApiProduct>
    @POST("api/v1/estante/produtos") suspend fun saveProducts(@Body body: JsonObject): ApiSavedProducts
    @DELETE("api/v1/estante/produtos/{id}") suspend fun removeProduct(@Path("id") id: Int)
    @GET("api/v1/estante/substancias") suspend fun substances(): ApiSubstances
    @GET("api/v1/estante/produtos/{id}/resumo-simplificado") suspend fun summary(@Path("id") id: Int): ApiSummary
    @GET("api/v1/estante/produtos/{id}/primeiros-socorros") suspend fun firstAid(@Path("id") id: Int): ApiFirstAid
    @GET("api/v1/comodos") suspend fun rooms(@Query("page") page: Int, @Query("size") size: Int = 50): ApiPage<ApiRoomSummary>
    @GET("api/v1/comodos/{id}") suspend fun room(@Path("id") id: Int): ApiRoom
    @GET("api/v1/estante/produtos/{id}/comodo") suspend fun productRoom(@Path("id") id: Int): ApiRoomSummary
    @POST("api/v1/comodos") suspend fun createRoom(@Body body: JsonObject): ApiRoom
    @POST("api/v1/comodos/{id}/produtos") suspend fun addRoomProducts(@Path("id") id: Int, @Body body: JsonObject): ApiRoom
    @DELETE("api/v1/comodos/{id}/produtos/{productId}") suspend fun removeRoomProduct(@Path("id") id: Int, @Path("productId") productId: Int)
    @GET("api/v1/pontos") suspend fun points(@Query("page") page: Int, @Query("size") size: Int = 50): ApiPage<ApiPoint>
    @GET("api/v1/misturas") suspend fun mixtures(@Query("page") page: Int, @Query("size") size: Int = 50,
        @Query("produtoId") productId: Int? = null): ApiPage<ApiMixture>
    @POST("api/v1/misturas") suspend fun mix(@Body body: JsonObject): ApiMixture
    @POST("api/v1/chat/mensagens") suspend fun chat(@Body body: JsonObject): ApiChatReply
}
