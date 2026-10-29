package com.quimia.android.presentation.screens.shelf

import com.quimia.android.data.catalog.*
import com.quimia.android.data.shelf.*
import com.quimia.android.presentation.screens.catalog.CatalogProblem
import com.quimia.android.presentation.screens.catalog.catalogProblem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

internal data class ShelfLoad<T>(val loading: Boolean = true, val data: T? = null, val error: CatalogProblem? = null)
internal data class ShelfState(val query: String = "", val substancesTab: Boolean = false, val order: String? = "alfabetica",
    val products: List<ApiCatalogProduct> = emptyList(), val substances: List<ShelfApiSubstance> = emptyList(),
    val loading: Boolean = true, val error: CatalogProblem? = null, val nextPage: Int? = null,
    val moreLoading: Boolean = false, val moreError: CatalogProblem? = null,
    val protection: Map<Int, ShelfLoad<ShelfFirstAid>> = emptyMap(),
    val infoId: Int? = null, val summary: ShelfLoad<ShelfSummary> = ShelfLoad(),
    val roomId: Int? = null, val room: ShelfLoad<ShelfRoom> = ShelfLoad(),
    val historyId: Int? = null, val history: List<ShelfMixture> = emptyList(), val historyLoading: Boolean = false,
    val historyError: CatalogProblem? = null, val historyNext: Int? = null,
    val removingId: Int? = null, val removedId: Int? = null, val removeError: CatalogProblem? = null, val notice: String? = null)

internal class ShelfController(private val source: ShelfDataSource, private val user: String) {
    private val mutable = MutableStateFlow(ShelfState())
    val state = mutable.asStateFlow()
    private var revision = 0
    private var infoRevision = 0
    private var roomRevision = 0
    private var historyRevision = 0
    private val moreLock = Mutex()
    private val removeLock = Mutex()
    private val historyMoreLock = Mutex()
    fun setQuery(query: String) {
        if (state.value.query == query.take(100)) return
        invalidate { it.copy(query = query.take(100)) }
    }
    fun setTab(substances: Boolean) { if (state.value.substancesTab != substances) invalidate { it.copy(substancesTab = substances, order = "alfabetica") } }
    fun setSort(sort: ShelfSort?) {
        if (state.value.substancesTab && sort != null && sort != ShelfSort.Alphabetical) { notice("Este filtro ainda não está disponível para substâncias."); return }
        val order = when(sort) { ShelfSort.Alphabetical -> "alfabetica"; ShelfSort.Recent -> "recentes"; ShelfSort.Oldest -> "antigos"; ShelfSort.Favorites -> "favoritos"; null -> null }
        if (order != state.value.order) invalidate { it.copy(order = order) }
    }
    private fun invalidate(change: (ShelfState) -> ShelfState) {
        revision++
        mutable.update { change(it).copy(products = emptyList(), substances = emptyList(), loading = true, error = null,
            nextPage = null, moreLoading = false, moreError = null) }
    }
    suspend fun load() {
        val request = ++revision
        val before = state.value
        mutable.update { it.copy(loading = true, error = null, moreLoading = false, moreError = null, nextPage = null) }
        try {
            if (before.substancesTab) {
                val items = source.substances(user, before.query, before.order)
                if (request == revision) mutable.update { it.copy(substances = items, loading = false) }
            } else {
                val page = source.products(user, before.query, before.order, 0)
                validateCatalogPage(page, 0, 20)
                if (request == revision) mutable.update { it.copy(products = page.itens, loading = false,
                    nextPage = 1.takeIf { page.totalPaginas > 1 }) }
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (request == revision) mutable.update { it.copy(loading = false, error = catalogProblem(error)) } }
    }
    suspend fun loadMore() {
        if (!moreLock.tryLock()) return
        try {
            val before = state.value
            val pageNumber = before.nextPage ?: return
            if (before.loading || before.substancesTab) return
            val request = revision
            mutable.update { it.copy(moreLoading = true, moreError = null) }
            try {
                val page = source.products(user, before.query, before.order, pageNumber)
                validateCatalogPage(page, pageNumber, 20)
                if (request == revision) mutable.update { it.copy(products = (it.products + page.itens).distinctBy { product -> product.id },
                    moreLoading = false, nextPage = (pageNumber + 1).takeIf { it < page.totalPaginas }) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { if (request == revision) mutable.update { it.copy(moreLoading = false, moreError = catalogProblem(error)) } }
        } finally { moreLock.unlock() }
    }
    suspend fun protect(id: Int, force: Boolean = false) {
        val existing = state.value.protection[id]
        if (existing != null && (existing.loading || (!force && existing.data != null))) return
        mutable.update { it.copy(protection = it.protection + (id to ShelfLoad())) }
        try {
            val data = source.firstAid(user, id)
            mutable.update { it.copy(protection = it.protection + (id to ShelfLoad(loading = false, data = data))) }
        } catch (cancelled: CancellationException) {
            mutable.update { it.copy(protection = it.protection - id) }; throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(protection = it.protection + (id to ShelfLoad(loading = false, error = catalogProblem(error)))) } }
    }
    suspend fun showInformation(id: Int) {
        val request = ++infoRevision
        mutable.update { it.copy(infoId = id, summary = ShelfLoad()) }
        try {
            val summary = source.summary(user, id)
            if (request == infoRevision) mutable.update { it.copy(summary = ShelfLoad(loading = false, data = summary)) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (request == infoRevision) mutable.update { it.copy(summary = ShelfLoad(loading = false, error = catalogProblem(error))) } }
    }
    fun closeInformation() { infoRevision++; mutable.update { it.copy(infoId = null) } }
    suspend fun showRoom(id: Int) {
        val request = ++roomRevision
        mutable.update { it.copy(roomId = id, room = ShelfLoad()) }
        try {
            val room = source.room(user, id)
            if (request == roomRevision) mutable.update { it.copy(room = ShelfLoad(loading = false, data = room)) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (request == roomRevision) mutable.update { it.copy(room = ShelfLoad(loading = false, error = catalogProblem(error))) } }
    }
    fun closeRoom() { roomRevision++; mutable.update { it.copy(roomId = null) } }
    suspend fun showHistory(id: Int) {
        val request = ++historyRevision
        mutable.update { it.copy(historyId = id, history = emptyList(), historyLoading = true, historyError = null, historyNext = null) }
        fetchHistory(id, 0, request)
    }
    suspend fun moreHistory() {
        if (!historyMoreLock.tryLock()) return
        try {
            val before = state.value
            val id = before.historyId ?: return
            if (before.historyLoading) return
            val next = before.historyNext ?: return
            mutable.update { it.copy(historyLoading = true, historyError = null) }
            fetchHistory(id, next, historyRevision)
        } finally { historyMoreLock.unlock() }
    }
    private suspend fun fetchHistory(id: Int, page: Int, request: Int) {
        try {
            val result = source.history(user, id, page)
            if (request == historyRevision) mutable.update { it.copy(historyLoading = false,
                history = (if (page == 0) result.itens else it.history + result.itens).distinctBy { mixture -> mixture.id },
                historyNext = (page + 1).takeIf { it < result.totalPaginas }) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (request == historyRevision) mutable.update { it.copy(historyLoading = false, historyError = catalogProblem(error)) } }
    }
    fun closeHistory() { historyRevision++; mutable.update { it.copy(historyId = null, historyLoading = false) } }
    suspend fun remove(id: Int) {
        if (!removeLock.tryLock()) return
        try {
            mutable.update { it.copy(removingId = id, removedId = null, removeError = null) }
            source.remove(user, id)
            revision++
            infoRevision++; roomRevision++; historyRevision++
            mutable.update { it.copy(removingId = null, removedId = id, products = it.products.filterNot { product -> product.id == id },
                substances = emptyList(), protection = it.protection - id, infoId = null, roomId = null, historyId = null,
                notice = "Produto removido da sua estante.") }
            load()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(removeError = catalogProblem(error)) }
        } finally { mutable.update { it.copy(removingId = null) }; removeLock.unlock() }
    }
    fun clearRemoval() { mutable.update { it.copy(removeError = null, removedId = null) } }
    fun notice(message: String?) { mutable.update { it.copy(notice = message) } }
}
