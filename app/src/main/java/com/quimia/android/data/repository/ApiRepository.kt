package com.quimia.android.data.repository

import com.quimia.android.data.remote.ApiDecoder
import com.quimia.android.data.remote.ApiException
import com.quimia.android.data.remote.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class ApiRepository {
    protected suspend inline fun <reified T> safeApiCall(
        crossinline apiCall: suspend () -> Response<T>
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error(ApiException("Resposta vazia da API"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                ApiResult.Error(ApiException("Erro ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiException("Erro de conexão", e))
        }
    }

    protected suspend inline fun <reified T> safeApiCallDirect(
        crossinline apiCall: suspend () -> T
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = apiCall()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error(ApiException("Erro na requisição", e))
        }
    }
}
