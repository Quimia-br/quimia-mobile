package com.quimia.android.data.kemi

import com.quimia.android.data.auth.AuthRepository
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

internal const val KEMI_MESSAGE_LIMIT = 2000

@Serializable internal data class KemiMessageRequest(val texto: String, val sessaoId: String)
@Serializable internal data class KemiMessageResponse(val texto: String, val sessaoId: String)
@Serializable internal data class KemiCloseResponse(val sessaoId: String, val situacao: String)

internal interface KemiApi {
    @POST("api/v1/chat/mensagens") suspend fun send(@Header("Authorization") bearer: String,
        @Body body: KemiMessageRequest): Response<KemiMessageResponse>
    @POST("api/v1/chat/sessoes/{id}/encerrar") suspend fun close(@Header("Authorization") bearer: String,
        @Path("id") sessionId: String): Response<KemiCloseResponse>
}

internal class KemiContractException : Exception()

/** Only the mobile API receives the user's JWT. Agent service credentials never belong in the APK. */
internal class KemiRepository(private val api: KemiApi, private val sessions: AuthRepository) {
    suspend fun send(userId: String, sessionId: String, text: String): String {
        require(validSession(sessionId))
        val message = text.trim()
        require(message.isNotEmpty() && message.length <= KEMI_MESSAGE_LIMIT)
        return sessions.withSession(userId) { bearer ->
            val answer = confirmed(api.send(bearer, KemiMessageRequest(message, sessionId)))
            if (answer.sessaoId != sessionId || answer.texto.isBlank() ||
                Regex("(?is)^\\s*<(?:!doctype\\s+html|html\\b)").containsMatchIn(answer.texto))
                throw KemiContractException()
            answer.texto
        }
    }

    suspend fun close(userId: String, sessionId: String) {
        require(validSession(sessionId))
        sessions.withSession(userId) { bearer ->
            val result = confirmed(api.close(bearer, sessionId))
            if (result.sessaoId != sessionId || result.situacao !in setOf("resumida", "ja_resumida", "sem_turnos"))
                throw KemiContractException()
        }
    }

    private fun <T> confirmed(response: Response<T>): T {
        if (!response.isSuccessful) throw HttpException(response)
        if (response.code() != 200 || response.headers()["Content-Type"]?.substringBefore(';')?.trim()
                ?.equals("application/json", ignoreCase = true) != true) throw KemiContractException()
        return response.body() ?: throw KemiContractException()
    }
    private fun validSession(id: String) = Regex("[A-Za-z0-9_-]{1,64}").matches(id)
}
