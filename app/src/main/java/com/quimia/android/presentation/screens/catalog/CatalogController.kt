package com.quimia.android.presentation.screens.catalog

import com.quimia.android.data.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

internal data class CatalogProblem(val kind: GenericErrorKind, val message: String)
internal data class CatalogState(
    val query: String = "", val products: List<ApiCatalogProduct> = emptyList(),
    val loading: Boolean = true, val error: CatalogProblem? = null, val nextPage: Int? = null,
    val moreLoading: Boolean = false, val moreError: CatalogProblem? = null,
    val savedIds: Set<Int> = emptySet(), val shelfLoading: Boolean = true, val shelfReady: Boolean = false,
    val shelfError: CatalogProblem? = null, val savingIds: Set<Int> = emptySet(),
    val saveError: CatalogProblem? = null, val notice: String? = null,
    val noticeSuccess: Boolean = false,
    val detailOpen: Boolean = false, val detail: ApiCatalogProduct? = null,
    val detailLoading: Boolean = false, val detailError: CatalogProblem? = null,
)

internal class CatalogController(private val source: CatalogDataSource, private val userId: String) {
    private val mutable = MutableStateFlow(CatalogState())
    val state = mutable.asStateFlow()
    private var searchRevision = 0
    private var detailRevision = 0
    private val saveLock = Mutex()
    private val shelfLock = Mutex()
    private val moreLock = Mutex()
    private var lastDetail: Int? = null
    private var lastBarcode: String? = null
    private var lastSaveIds: List<Int> = emptyList()

    fun setQuery(value: String) {
        val query = value.take(100)
        if (query == state.value.query) return
        searchRevision++
        mutable.update { it.copy(query = query, products = emptyList(), loading = true, error = null,
            nextPage = null, moreLoading = false, moreError = null) }
    }
    suspend fun search() {
        val revision = ++searchRevision
        val query = state.value.query
        mutable.update { it.copy(loading = true, error = null, nextPage = null, moreLoading = false, moreError = null) }
        try {
            val page = source.products(userId, query, 0, 20)
            validateCatalogPage(page, 0, 20)
            if (revision == searchRevision) mutable.update { it.copy(products = page.itens,
                loading = false, nextPage = 1.takeIf { page.totalPaginas > 1 }) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) {
            if (revision == searchRevision) mutable.update { it.copy(loading = false, error = catalogProblem(error)) }
        }
    }
    suspend fun loadShelf() {
        if (!shelfLock.tryLock()) return
        try {
            mutable.update { it.copy(shelfLoading = true, shelfError = null) }
            val ids = source.savedIds(userId)
            mutable.update { it.copy(savedIds = ids, shelfLoading = false, shelfReady = true) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(shelfLoading = false, shelfReady = false, shelfError = catalogProblem(error)) }
        } finally { shelfLock.unlock() }
    }
    suspend fun loadMore() {
        if (!moreLock.tryLock()) return
        try {
            val before = state.value
            val pageNumber = before.nextPage ?: return
            if (before.loading) return
            val revision = searchRevision
            mutable.update { it.copy(moreLoading = true, moreError = null) }
            try {
                val page = source.products(userId, before.query, pageNumber, 20)
                validateCatalogPage(page, pageNumber, 20)
                if (revision == searchRevision) mutable.update { current ->
                    current.copy(products = (current.products + page.itens).distinctBy { it.id }, moreLoading = false,
                        nextPage = (pageNumber + 1).takeIf { it < page.totalPaginas })
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                if (revision == searchRevision) mutable.update { it.copy(moreLoading = false, moreError = catalogProblem(error)) }
            }
        } finally { moreLock.unlock() }
    }
    suspend fun save(ids: List<Int>) {
        if (!saveLock.tryLock()) return
        try {
            if (!state.value.shelfReady || state.value.shelfLoading) return
            val requested = ids.distinct().filterNot { it in state.value.savedIds }
            if (requested.isEmpty()) return
            if (requested.size > 50) { mutable.update { it.copy(notice = "Selecione até 50 produtos por vez.") }; return }
            lastSaveIds = requested
            mutable.update { it.copy(savingIds = requested.toSet(), saveError = null, notice = null) }
            try {
                val result = source.save(userId, requested)
                val confirmed = result.filter { it.status in setOf("adicionado", "ja_na_estante") }.map { it.produtoId }.toSet()
                val missing = result.count { it.status == "nao_encontrado" }
                val message = when {
                    missing > 0 -> "${confirmed.size} produto(s) confirmado(s) na estante. $missing não encontrado(s) no catálogo."
                    result.all { it.status == "ja_na_estante" } -> "Os produtos já estavam na sua estante."
                    else -> "${confirmed.size} produto(s) confirmado(s) na sua estante."
                }
                mutable.update { it.copy(savedIds = it.savedIds + confirmed, notice = message, noticeSuccess = missing == 0) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { mutable.update { it.copy(saveError = catalogProblem(error)) }
            } finally { mutable.update { it.copy(savingIds = emptySet()) } }
        } finally { saveLock.unlock() }
    }
    suspend fun retrySave() { save(lastSaveIds) }
    suspend fun openDetail(id: Int) { lastDetail = id; lastBarcode = null; fetchDetail { source.detail(userId, id) } }
    suspend fun findBarcode(code: String) { lastBarcode = code; lastDetail = null; fetchDetail { source.barcode(userId, code) } }
    suspend fun retryDetail() { lastBarcode?.let { findBarcode(it); return }; lastDetail?.let { openDetail(it) } }
    private suspend fun fetchDetail(request: suspend () -> ApiCatalogProduct) {
        val revision = ++detailRevision
        mutable.update { it.copy(detailOpen = true, detail = null, detailLoading = true, detailError = null) }
        try {
            val detail = request().also(::validateCatalogProduct)
            if (revision == detailRevision) mutable.update { it.copy(detail = detail, detailLoading = false) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) {
            if (revision == detailRevision) mutable.update { it.copy(detailLoading = false, detailError = catalogProblem(error)) }
        }
    }
    fun closeDetail() { detailRevision++; mutable.update { it.copy(detailOpen = false, detail = null, detailError = null, detailLoading = false) } }
    fun dismissNotice() { mutable.update { it.copy(notice = null, saveError = null) } }
}

internal fun catalogProblem(error: Exception): CatalogProblem {
    val kind = when (error) {
        is CatalogSessionExpired -> GenericErrorKind.SessionExpired
        is SocketTimeoutException -> GenericErrorKind.Timeout
        is IOException -> GenericErrorKind.Offline
        is HttpException -> when (error.code()) { 401 -> GenericErrorKind.SessionExpired; 403 -> GenericErrorKind.AccessDenied
            404 -> GenericErrorKind.ContentUnavailable; in 500..599 -> GenericErrorKind.ServiceUnavailable; else -> GenericErrorKind.Unknown }
        else -> GenericErrorKind.Unknown
    }
    val message = when {
        error is CatalogContractException -> "A API retornou dados incompletos ou incompatíveis. Não foi possível confirmar esta operação."
        error is HttpException && error.code() == 429 -> "Muitas solicitações. Aguarde um pouco e tente novamente."
        else -> kind.description.replace("\n", " ")
    }
    return CatalogProblem(kind, message)
}
