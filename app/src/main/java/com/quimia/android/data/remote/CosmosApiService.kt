package com.quimia.android.data.remote

import com.quimia.android.data.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CosmosApiService {
    @GET("products/search")
    suspend fun searchByBarcode(
        @Query("barcode") barcode: String,
        @Header("Authorization") token: String,
        @Header("User-Agent") userAgent: String = "Cosmos-API-Request"
    ): Response<ProductResponse>
}
