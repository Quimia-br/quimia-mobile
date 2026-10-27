package com.quimia.android.data.home

import com.quimia.android.data.auth.AuthRepository
import com.quimia.android.data.auth.SessionState
import com.quimia.android.data.auth.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@Serializable internal data class HomePage<T>(val itens: List<T>, val pagina: Int, val tamanho: Int,
    val totalItens: Long, val totalPaginas: Int)
@Serializable internal data class HomeProduct(val id: Int, val nome: String, val fotoUrl: String? = null)
@Serializable internal data class HomeDose(val produtoA: String? = null, val produtoB: String? = null)
@Serializable internal data class HomeMixture(val id: Int, val produtoA: HomeProduct?, val produtoB: HomeProduct?,
    val resultado: String, val severidade: String? = null, val descricaoRisco: String? = null,
    val consultadoEm: String? = null, val dosagem: HomeDose? = null)

internal interface HomeApi {
    @GET("api/v1/usuarios/me") suspend fun profile(@Header("Authorization") bearer: String): UserProfile
    @GET("api/v1/misturas") suspend fun history(@Header("Authorization") bearer: String,
        @Query("page") page: Int, @Query("size") size: Int): HomePage<HomeMixture>
}

internal interface HomeDataSource {
    suspend fun profile(userId: String): UserProfile
    suspend fun history(userId: String, page: Int, size: Int): HomePage<HomeMixture>
}
internal class HomeContractException(message: String) : Exception(message)
internal class HomeSessionExpired : Exception()

internal class HomeRepository(private val api: HomeApi, private val sessions: AuthRepository) : HomeDataSource {
    private suspend fun <T> authenticated(userId: String, request: suspend (String) -> T): T = try {
        sessions.withSession(userId, request)
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) {
        if (sessions.session.value == SessionState.SignedOut) throw HomeSessionExpired()
        throw error
    }
    override suspend fun profile(userId: String): UserProfile = authenticated(userId) { bearer ->
        api.profile(bearer).also {
            if (it.id != userId || it.nome.isBlank() || it.email.isBlank())
                throw HomeContractException("GET /usuarios/me não retornou o usuário da sessão.")
        }
    }
    override suspend fun history(userId: String, page: Int, size: Int): HomePage<HomeMixture> =
        authenticated(userId) { api.history(it, page, size) }
}
