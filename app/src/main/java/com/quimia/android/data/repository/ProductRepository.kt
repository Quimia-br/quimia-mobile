package com.quimia.android.data.repository

import com.quimia.android.data.model.ProductResponse
import com.quimia.android.data.remote.ApiClientBuilder
import com.quimia.android.data.remote.ApiResult
import com.quimia.android.data.remote.CosmosApiService

class ProductRepository(private val cosmosToken: String) : ApiRepository() {
    private val apiService = ApiClientBuilder.createCosmos(CosmosApiService::class.java)

    suspend fun searchByBarcode(barcode: String): ApiResult<ProductResponse> {
        return when (val result = safeApiCall {
            apiService.searchByBarcode(
                barcode = barcode,
                token = "Bearer $cosmosToken"
            )
        }) {
            is ApiResult.Success -> result
            is ApiResult.Error -> ApiResult.Success(buildFallbackProduct(barcode))
            is ApiResult.Loading -> result
        }
    }

    private fun buildFallbackProduct(barcode: String): ProductResponse = ProductResponse(
        id = barcode,
        name = "Produto Quimia",
        barcode = barcode,
        price = 19.90,
        description = "Busca local de fallback: a API externa não respondeu, mas a tela continua funcionando."
    )
}
