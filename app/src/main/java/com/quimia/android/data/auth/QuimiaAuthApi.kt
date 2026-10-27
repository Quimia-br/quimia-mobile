package com.quimia.android.data.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.io.IOException

@Serializable class LoginRequest(val email: String, val senha: String)
@Serializable class FirebaseLoginRequest(val idToken: String)
@Serializable class RefreshRequest(val refreshToken: String)
@Serializable data class UserProfile(val id: String, val nome: String, val email: String,
    val dataNasc: String? = null, val fotoUrl: String? = null)
@Serializable class MobileSessionResponse(val accessToken: String, val refreshToken: String,
    val tokenType: String, val expiresIn: Long, val user: UserProfile)

interface QuimiaAuthApi {
    @POST("api/v1/auth/mobile/login") suspend fun login(@Body body: LoginRequest): MobileSessionResponse
    @POST("api/v1/auth/firebase") suspend fun firebase(@Body body: FirebaseLoginRequest): MobileSessionResponse
    @POST("api/v1/auth/mobile/refresh") suspend fun refresh(@Body body: RefreshRequest): MobileSessionResponse
    @POST("api/v1/auth/mobile/logout") suspend fun logout(@Header("Authorization") bearer: String)
    @GET("api/v1/usuarios/me") suspend fun me(@Header("Authorization") bearer: String): UserProfile
}

object AuthApiFactory {
    val json = Json { ignoreUnknownKeys = true }
    fun create(baseUrl: String): QuimiaAuthApi {
        val client = OkHttpClient.Builder().retryOnConnectionFailure(false)
            .followRedirects(false).followSslRedirects(false)
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().tag(AuthAttempt::class.java, AuthAttempt()).build())
            }
            .addNetworkInterceptor { chain ->
                if (chain.request().tag(AuthAttempt::class.java)?.sent?.getAndSet(true) == true)
                    throw IOException("Authentication request replay prevented")
                chain.proceed(chain.request())
            }
            .callTimeout(30, TimeUnit.SECONDS).build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(QuimiaAuthApi::class.java)
    }
    private class AuthAttempt(val sent: AtomicBoolean = AtomicBoolean(false))
}
