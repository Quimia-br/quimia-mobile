package com.quimia.android.data.remote

import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClientBuilder {
    private const val BASE_URL_COSMOS = "https://api.cosmos.com/"
    private const val BASE_URL_DEFAULT = "https://api.example.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val cosmosRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_COSMOS)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val defaultRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_DEFAULT)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun <T> createCosmos(service: Class<T>): T = cosmosRetrofit.create(service)
    fun <T> create(service: Class<T>): T = defaultRetrofit.create(service)
}
