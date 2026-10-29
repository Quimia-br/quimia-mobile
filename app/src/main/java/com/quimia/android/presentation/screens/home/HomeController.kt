package com.quimia.android.presentation.screens.home

import com.quimia.android.data.auth.UserProfile
import com.quimia.android.data.home.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.URI
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

internal data class HomeState(val profile: UserProfile? = null, val profileLoading: Boolean = true,
    val profileError: GenericErrorKind? = null, val history: List<HomeMixture> = emptyList(),
    val historyLoading: Boolean = true, val historyError: GenericErrorKind? = null,
    val nextPage: Int? = null, val moreLoading: Boolean = false, val moreError: GenericErrorKind? = null)

internal fun homeProblem(error: Exception): GenericErrorKind = when (error) {
    is HomeSessionExpired -> GenericErrorKind.SessionExpired
    is InterruptedIOException -> GenericErrorKind.Timeout
    is IOException -> GenericErrorKind.Offline
    is HttpException -> when (error.code()) {
        401 -> GenericErrorKind.SessionExpired
        403 -> GenericErrorKind.AccessDenied
        404 -> GenericErrorKind.ContentUnavailable
        in 500..599 -> GenericErrorKind.ServiceUnavailable
        else -> GenericErrorKind.Unknown
    }
    else -> GenericErrorKind.Unknown
}

internal fun homeTimestamp(value: String): Long? {
    val parts = Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d{1,9}))?(Z|[+-]\\d{2}:\\d{2})$")
        .matchEntire(value) ?: return null
    val normalized = parts.groupValues[1] + "." + parts.groupValues[2].padEnd(3, '0').take(3) + parts.groupValues[3]
    val position = ParsePosition(0)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).apply { isLenient = false }
    return format.parse(normalized, position)?.takeIf { position.index == normalized.length }?.time
}

internal fun homePhotoUrl(value: String?): String? = value?.takeIf { url ->
    runCatching { URI(url).let { it.scheme.equals("https", true) && !it.host.isNullOrBlank() } }.getOrDefault(false)
}

internal fun validateHomePage(value: HomePage<HomeMixture>, page: Int, size: Int): HomePage<HomeMixture> {
    if (value.pagina != page || value.tamanho != size || value.totalItens < value.itens.size || value.totalPaginas < 0 ||
        value.itens.size > size || (value.itens.isNotEmpty() && value.totalPaginas <= page) ||
        value.itens.distinctBy { it.id }.size != value.itens.size)
        throw HomeContractException("GET /misturas retornou uma página inconsistente.")
    value.itens.forEach {
        if (it.id <= 0 || it.produtoA == null || it.produtoB == null || it.produtoA.id <= 0 || it.produtoB.id <= 0 ||
            it.produtoA.nome.isBlank() || it.produtoB.nome.isBlank() || it.resultado.isBlank() ||
            (it.consultadoEm != null && homeTimestamp(it.consultadoEm) == null))
            throw HomeContractException("GET /misturas não retornou os dados necessários para o histórico.")
    }
    return value
}

internal class HomeController(private val source: HomeDataSource, private val userId: String) {
    private val operation = Mutex()
    private val mutableState = MutableStateFlow(HomeState())
    val state = mutableState.asStateFlow()
    private fun update(transform: (HomeState) -> HomeState) { mutableState.update(transform) }
    suspend fun refresh() {
        if (!operation.tryLock()) return
        try {
            update { it.copy(profileLoading = true, historyLoading = true, profileError = null, historyError = null, moreError = null) }
            coroutineScope {
                launch {
                    try {
                        val profile = source.profile(userId)
                        if (profile.id != userId || profile.nome.isBlank() || profile.email.isBlank()) throw HomeContractException("Usuário incompatível.")
                        update { it.copy(profile = profile, profileLoading = false) }
                    } catch (cancelled: CancellationException) { throw cancelled
                    } catch (error: Exception) { update { it.copy(profileLoading = false, profileError = homeProblem(error)) } }
                }
                launch {
                    try {
                        val page = validateHomePage(source.history(userId, 0, 10), 0, 10)
                        update { it.copy(history = page.itens, historyLoading = false,
                            nextPage = if (page.totalPaginas > 1) 1 else null) }
                    } catch (cancelled: CancellationException) { throw cancelled
                    } catch (error: Exception) { update { it.copy(historyLoading = false, historyError = homeProblem(error)) } }
                }
            }
        } finally { operation.unlock() }
    }
    suspend fun loadMore() {
        if (!operation.tryLock()) return
        try {
            val next = mutableState.value.nextPage ?: return
            update { it.copy(moreLoading = true, moreError = null) }
            try {
                val page = validateHomePage(source.history(userId, next, 10), next, 10)
                update { current ->
                    val combined = LinkedHashMap<Int, HomeMixture>()
                    (current.history + page.itens).forEach { combined[it.id] = it }
                    current.copy(history = combined.values.toList(), moreLoading = false,
                        nextPage = if (next + 1 < page.totalPaginas) next + 1 else null)
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { update { it.copy(moreLoading = false, moreError = homeProblem(error)) } }
        } finally { operation.unlock() }
    }
}
