package com.quimia.android.data.repository

import com.quimia.android.data.model.User
import com.quimia.android.data.remote.ApiClientBuilder
import com.quimia.android.data.remote.ApiResult
import com.quimia.android.data.remote.UserApiService

class UserRepository : ApiRepository() {
    private val apiService = ApiClientBuilder.create(UserApiService::class.java)

    suspend fun getUsers(): ApiResult<List<User>> {
        return safeApiCall {
            apiService.getUsers()
        }
    }

    suspend fun getUserById(id: Int): ApiResult<User> {
        return safeApiCall {
            apiService.getUserById(id)
        }
    }
}
