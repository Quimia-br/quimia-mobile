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
import retrofit2.http.PATCH
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.io.IOException

@Serializable class LoginRequest(val email: String, val senha: String)
@Serializable class FirebaseLoginRequest(val idToken: String)
@Serializable class RefreshRequest(val refreshToken: String)
@Serializable class RegisterRequest(val nome: String, val email: String, val senha: String,
    val dataNasc: String, val localizacao: AuthAddress? = null)
@Serializable data class AuthAddress(val cep: String, val estado: String, val bairro: String,
    val rua: String?, val numero: Int, val complemento: String? = null)
@Serializable class InitialProfileRequest(val nome: String, val dataNasc: String)
@Serializable class RecoveryEmailRequest(val email: String)
@Serializable class RecoveryCodeRequest(val email: String, val code: String, val challengeToken: String)
@Serializable class ResetPasswordRequest(val resetToken: String, val novaSenha: String)
@Serializable class RecoveryChallenge(val challengeToken: String, val expiresIn: Long)
@Serializable class RecoveryGrant(val resetToken: String, val expiresIn: Long)
@Serializable data class UserProfile(val id: String, val nome: String, val email: String,
    val dataNasc: String? = null, val fotoUrl: String? = null)
@Serializable class MobileSessionResponse(val accessToken: String, val refreshToken: String,
    val tokenType: String, val expiresIn: Long, val user: UserProfile)

interface QuimiaAuthApi {
    @POST("api/v1/auth/register") suspend fun register(@Body body: RegisterRequest): UserProfile
    @POST("api/v1/auth/forgot-password") suspend fun forgot(@Body body: RecoveryEmailRequest): RecoveryChallenge
    @POST("api/v1/auth/verify-recovery-code") suspend fun verifyCode(@Body body: RecoveryCodeRequest): RecoveryGrant
    @POST("api/v1/auth/reset-password") suspend fun reset(@Body body: ResetPasswordRequest)
    @POST("api/v1/auth/mobile/login") suspend fun login(@Body body: LoginRequest): MobileSessionResponse
    @POST("api/v1/auth/firebase") suspend fun firebase(@Body body: FirebaseLoginRequest): MobileSessionResponse
    @POST("api/v1/auth/mobile/refresh") suspend fun refresh(@Body body: RefreshRequest): MobileSessionResponse
    @POST("api/v1/auth/mobile/logout") suspend fun logout(@Header("Authorization") bearer: String)
    @GET("api/v1/usuarios/me") suspend fun me(@Header("Authorization") bearer: String): UserProfile
    @PATCH("api/v1/usuarios/me") suspend fun completeProfile(@Header("Authorization") bearer: String,
        @Body body: InitialProfileRequest): UserProfile
    @PUT("api/v1/usuarios/me/localizacao") suspend fun saveAddress(@Header("Authorization") bearer: String,
        @Body body: AuthAddress): AuthAddress
}

object AuthApiFactory {
    val json = Json { ignoreUnknownKeys = true }
    fun create(baseUrl: String): QuimiaAuthApi = create(baseUrl, QuimiaAuthApi::class.java)
    fun <T> create(baseUrl: String, service: Class<T>, timeoutSeconds: Long = 30): T {
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
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS).readTimeout(timeoutSeconds, TimeUnit.SECONDS).build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(service)
    }
    private class AuthAttempt(val sent: AtomicBoolean = AtomicBoolean(false))
}
