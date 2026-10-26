package com.quimia.android.data.project

import android.content.Context
import com.quimia.android.BuildConfig
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

fun apiBody(vararg fields: Pair<String, String>) = buildJsonObject { fields.forEach { (key, value) -> put(key, value) } }
fun apiIds(ids: List<String>) = buildJsonObject { put("produtoIds", JsonArray(ids.distinct().map { JsonPrimitive(it.toInt()) })) }

fun isApiProblem(error: Throwable, status: Int, detail: String): Boolean {
    if (error !is HttpException || error.code() != status) return false
    return runCatching {
        Json.parseToJsonElement(error.response()?.errorBody()?.source()?.peek()?.readUtf8().orEmpty())
            .jsonObject["detail"]?.jsonPrimitive?.content == detail
    }.getOrDefault(false)
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
fun apiErrorMessage(error: Throwable): String = when (error) {
    is ApiContractException -> error.message ?: "Resposta incompatível com o contrato da API."
    is kotlinx.serialization.MissingFieldException -> "A API não retornou os campos obrigatórios: ${error.missingFields.joinToString()}."
    is kotlinx.serialization.SerializationException -> "Resposta incompatível com o contrato: um campo obrigatório está ausente ou possui tipo inválido."
    is HttpException -> {
        val text = error.response()?.errorBody()?.string().orEmpty()
        val body = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull()
        if (text.trimStart().startsWith("<")) "A API respondeu HTML em vez de JSON (HTTP ${error.code()}). O serviço pode estar em manutenção ou a publicação/rota está incorreta."
        else if (body?.get("errors") is JsonArray) (body.getValue("errors") as JsonArray)
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString("\n").take(1000)
        else when (error.code()) {
            401 -> "Sua sessão expirou ou os dados de acesso não conferem. Entre novamente."
            403 -> "Sua conta não tem acesso a esta operação."
            429 -> "Muitas tentativas. Aguarde um pouco e tente novamente."
            in 500..599 -> "A API está indisponível no momento. Tente novamente."
            else -> (body?.get("detail") as? JsonPrimitive)?.contentOrNull?.takeIf { it.length <= 400 }
                ?: "A API recusou a operação (HTTP ${error.code()}). Confira os dados."
        }
    }
    is IOException -> "Não foi possível conectar à API. Confira sua conexão e tente novamente."
    else -> "Não foi possível concluir a operação. Tente novamente."
}

private data class RequestOwner(val userId: String)

class ProjectClient(val sessions: SessionStorage, private val baseUrl: String = BuildConfig.QUIMIA_API_BASE_URL) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val anonymousClient = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()
    private fun buildApi(httpClient: OkHttpClient): ProjectApi = Retrofit.Builder().baseUrl(baseUrl).client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(ProjectApi::class.java)
    private val anonymousApi = buildApi(anonymousClient)
    val api: ProjectApi = buildApi(anonymousClient.newBuilder().addInterceptor { chain ->
        val request = chain.request()
        val timedChain = if (request.url.encodedPath == "/api/v1/chat/mensagens") chain.withReadTimeout(125, TimeUnit.SECONDS) else chain
        authorizedResponse(timedChain, request)
    }.authenticator { _, response ->
        if (response.request.header("Authorization") == null || response.priorResponse != null) null
        else synchronized(this) {
            val current = sessions.session.value
            if (current == null || response.request.tag(RequestOwner::class.java)?.userId != current.user.id) null
            else if (response.request.header("Authorization") != "Bearer ${current.accessToken}")
                response.request.newBuilder().header("Authorization", "Bearer ${current.accessToken}").build()
            else {
                val refreshed = runCatching { anonymousApi.refresh(apiBody("refreshToken" to current.refreshToken)).execute() }.getOrNull()
                val next = refreshed?.body()
                val stillCurrent = sessions.session.value == current
                if (next != null && refreshed.isSuccessful && stillCurrent && next.user.id == current.user.id) {
                    sessions.save(next)
                    response.request.newBuilder().header("Authorization", "Bearer ${next.accessToken}").build()
                } else {
                    if (stillCurrent && refreshed?.code() in listOf(400, 401, 403)) sessions.clear()
                    refreshed?.errorBody()?.close()
                    null
                }
            }
        }
    }.build())

    private fun authorizedResponse(chain: okhttp3.Interceptor.Chain, request: okhttp3.Request): okhttp3.Response {
        val session = sessions.session.value
        val authenticated = session != null && !request.url.encodedPath.startsWith("/api/v1/auth/")
        return chain.proceed(if (authenticated) request.newBuilder().header("Authorization", "Bearer ${session.accessToken}")
            .tag(RequestOwner::class.java, RequestOwner(session.user.id)).build() else request)
    }

    companion object {
        @Volatile private var shared: ProjectClient? = null
        fun get(context: Context): ProjectClient = shared ?: synchronized(this) {
            shared ?: ProjectClient(ProjectSessionStore(context.applicationContext)).also { shared = it }
        }
    }
}
