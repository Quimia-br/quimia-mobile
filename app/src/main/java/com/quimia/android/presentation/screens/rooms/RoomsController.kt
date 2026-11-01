package com.quimia.android.presentation.screens.rooms

import com.quimia.android.data.catalog.*
import com.quimia.android.data.rooms.*
import com.quimia.android.presentation.screens.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

internal enum class RoomsStage { List, Create, Detail, Adding, Editing, Saving, Success, Error }
internal data class RoomsState(val stage: RoomsStage = RoomsStage.List, val query: String = "",
    val rooms: List<ApiRoomSummary> = emptyList(), val loading: Boolean = true, val error: CatalogProblem? = null,
    val nextPage: Int? = null, val moreLoading: Boolean = false, val moreError: CatalogProblem? = null,
    val detailId: Int? = null, val detail: ApiRoom? = null, val detailLoading: Boolean = false, val detailError: CatalogProblem? = null,
    val available: List<ApiCatalogProduct> = emptyList(), val shelfLoading: Boolean = false, val shelfReady: Boolean = false,
    val shelfError: CatalogProblem? = null, val busy: Boolean = false, val mutationError: CatalogProblem? = null,
    val notice: String? = null)

internal class RoomsController(private val source: RoomsDataSource, private val user: String) {
    private val mutable = MutableStateFlow(RoomsState())
    val state = mutable.asStateFlow()
    private var listRevision = 0
    private var detailRevision = 0
    private var shelfRevision = 0
    private val moreLock = Mutex()
    private val mutationLock = Mutex()
    fun setQuery(query: String) {
        if (state.value.query == query.take(100)) return
        listRevision++
        mutable.update { it.copy(query = query.take(100), rooms = emptyList(), loading = true, error = null,
            nextPage = null, moreLoading = false, moreError = null) }
    }
    suspend fun loadList() {
        val revision = ++listRevision
        val query = state.value.query
        mutable.update { it.copy(loading = true, error = null, nextPage = null, moreLoading = false, moreError = null) }
        try {
            val page = source.list(user, query, 0); validateRoomPage(page, 0)
            if (revision == listRevision) mutable.update { it.copy(rooms = page.itens, loading = false, nextPage = 1.takeIf { page.totalPaginas > 1 }) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (revision == listRevision) mutable.update { it.copy(loading = false, error = roomsProblem(error)) } }
    }
    suspend fun loadMore() {
        if (!moreLock.tryLock()) return
        try {
            val before = state.value; val pageNumber = before.nextPage ?: return
            if (before.loading) return
            val revision = listRevision
            mutable.update { it.copy(moreLoading = true, moreError = null) }
            try {
                val page = source.list(user, before.query, pageNumber); validateRoomPage(page, pageNumber)
                if (revision == listRevision) mutable.update { it.copy(rooms = (it.rooms + page.itens).distinctBy { room -> room.id },
                    moreLoading = false, nextPage = (pageNumber + 1).takeIf { it < page.totalPaginas }) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { if (revision == listRevision) mutable.update { it.copy(moreLoading = false, moreError = roomsProblem(error)) } }
        } finally { moreLock.unlock() }
    }
    fun showList() {
        if (state.value.busy) return
        detailRevision++; shelfRevision++
        mutable.update { it.copy(stage = RoomsStage.List, mutationError = null) }
    }
    suspend fun prepareCreate() {
        if (state.value.busy) return
        detailRevision++
        mutable.update { it.copy(stage = RoomsStage.Create, mutationError = null, detail = null, detailId = null, detailLoading = false) }
        loadShelf()
    }
    suspend fun loadShelf() {
        val revision = ++shelfRevision
        mutable.update { it.copy(shelfLoading = true, shelfReady = false, shelfError = null) }
        try {
            val products = source.shelf(user)
            if (revision == shelfRevision) mutable.update { it.copy(available = products, shelfLoading = false, shelfReady = true) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (revision == shelfRevision) mutable.update { it.copy(shelfLoading = false, shelfReady = false, shelfError = roomsProblem(error)) } }
    }
    suspend fun open(id: Int) {
        if (state.value.busy) return
        val revision = ++detailRevision
        mutable.update { it.copy(stage = RoomsStage.Detail, detailId = id, detail = null, detailLoading = true,
            detailError = null, mutationError = null) }
        fetchDetail(id, revision)
    }
    private suspend fun fetchDetail(id: Int, revision: Int, removedId: Int? = null) {
        try {
            val room = source.detail(user, id); validateApiRoom(room)
            if (room.id != id || (removedId != null && room.produtos.any { it.id == removedId })) throw CatalogContractException()
            if (revision == detailRevision) mutable.update { it.copy(detail = room, detailLoading = false) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { if (revision == detailRevision) mutable.update { it.copy(detailLoading = false, detailError = roomsProblem(error)) } }
    }
    suspend fun create(name: String, icon: RoomIcon?, ids: List<Int>) {
        if (!mutationLock.tryLock()) return
        try {
            val before = state.value
            val error = apiRoomValidation(name, icon, ids, before.available.map { it.id })
            if (!before.shelfReady || before.shelfLoading || error != null) {
                notice(error ?: "Aguarde carregar os produtos da sua estante."); return
            }
            mutable.update { it.copy(stage = RoomsStage.Saving, busy = true, mutationError = null) }
            val room = source.create(user, normalizeRoomName(name), requireNotNull(icon).apiCode, ids.distinct())
            validateApiRoom(room)
            mutable.update { it.copy(stage = RoomsStage.Success, detailId = room.id, detail = room, busy = false) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(stage = RoomsStage.Error, mutationError = roomsProblem(error)) }
        } finally { mutable.update { it.copy(busy = false) }; mutationLock.unlock() }
    }
    fun reviseCreate() { mutable.update { it.copy(stage = RoomsStage.Create, mutationError = null) } }
    fun showCreated() { mutable.update { it.copy(stage = RoomsStage.Detail) } }
    suspend fun prepareAdd() {
        if (state.value.busy || state.value.detail == null) return
        mutable.update { it.copy(stage = RoomsStage.Adding, mutationError = null) }
        loadShelf()
    }
    suspend fun prepareEdit() {
        if (state.value.busy || state.value.detail == null) return
        mutable.update { it.copy(stage = RoomsStage.Editing, mutationError = null) }
        loadShelf()
    }
    suspend fun cancelAdd() {
        if (state.value.busy) return
        shelfRevision++
        val before = state.value
        if (before.mutationError != null && before.detailId != null) open(before.detailId)
        else mutable.update { it.copy(stage = RoomsStage.Detail, mutationError = null) }
    }
    suspend fun add(ids: List<Int>) {
        if (!mutationLock.tryLock()) return
        try {
            val before = state.value; val room = before.detail ?: return
            val existing = room.produtos.map { it.id }.toSet()
            val unique = ids.distinct()
            if (!before.shelfReady || before.shelfLoading || unique.isEmpty() || unique.any { id -> id in existing || before.available.none { it.id == id } } ||
                existing.size + unique.size > 20) { notice("Selecione produtos da sua estante, respeitando o limite de 20 no cômodo."); return }
            mutable.update { it.copy(busy = true, mutationError = null) }
            val updated = source.add(user, room.id, unique); validateApiRoom(updated)
            if (updated.id != room.id || !updated.produtos.map { it.id }.containsAll(existing + unique)) throw CatalogContractException()
            mutable.update { it.copy(detail = updated, stage = RoomsStage.Detail, notice = "Produtos adicionados ao cômodo.") }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(mutationError = roomsProblem(error)) }
        } finally { mutable.update { it.copy(busy = false) }; mutationLock.unlock() }
    }
    suspend fun remove(productId: Int) {
        if (!mutationLock.tryLock()) return
        try {
            val room = state.value.detail ?: return
            if (room.produtos.none { it.id == productId }) return
            mutable.update { it.copy(busy = true, mutationError = null) }
            source.remove(user, room.id, productId)
            val revision = ++detailRevision
            mutable.update { it.copy(detail = null, detailLoading = true, detailError = null, notice = "Produto removido do cômodo. Ele continua na sua estante.") }
            fetchDetail(room.id, revision, productId)
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(mutationError = roomsProblem(error)) }
        } finally { mutable.update { it.copy(busy = false) }; mutationLock.unlock() }
    }
    suspend fun edit(name: String, icon: RoomIcon, productIds: List<Int>? = null) {
        if (!mutationLock.tryLock()) return
        try {
            val room = state.value.detail ?: return
            val normalized = normalizeRoomName(name)
            if (normalized.isBlank() || normalized.length > 100) { notice("Informe um nome de até 100 caracteres."); return }
            val ids = productIds?.distinct()
            if (ids != null && (!state.value.shelfReady || state.value.shelfLoading || ids.size > 20 ||
                ids.any { id -> state.value.available.none { it.id == id } && room.produtos.none { it.id == id } })) {
                notice("Confira os produtos disponíveis na sua estante. O limite por cômodo é 20."); return
            }
            mutable.update { it.copy(busy = true, mutationError = null) }
            var updated = if (normalized != room.nome || icon.apiCode != room.icone)
                source.edit(user, room.id, normalized, icon.apiCode) else room
            validateApiRoom(updated)
            if (updated.id != room.id || updated.nome != normalized || updated.icone != icon.apiCode) throw CatalogContractException()
            mutable.update { it.copy(detail = updated) }
            if (ids != null) {
                val additions = ids.filterNot { id -> updated.produtos.any { it.id == id } }
                val removals = updated.produtos.map { it.id }.filterNot { it in ids }
                // Free capacity first when replacing products in a full room. Each confirmed step
                // becomes the next baseline, so retrying a partial save never repeats a removal.
                removals.forEach { id ->
                    source.remove(user, room.id, id)
                    updated = source.detail(user, room.id)
                    validateApiRoom(updated)
                    if (updated.id != room.id || updated.produtos.any { it.id == id }) throw CatalogContractException()
                    mutable.update { it.copy(detail = updated) }
                }
                if (additions.isNotEmpty()) {
                    updated = source.add(user, room.id, additions)
                    validateApiRoom(updated)
                }
                if (updated.id != room.id || updated.nome != normalized || updated.icone != icon.apiCode ||
                    updated.produtos.map { it.id }.toSet() != ids.toSet()) throw CatalogContractException()
            }
            mutable.update { it.copy(detail = updated, stage = RoomsStage.Detail, notice = "Cômodo atualizado.") }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) {
            val problem = roomsProblem(error)
            // A sequence of existing API routes is not atomic. Reconcile partial saves and keep
            // the form open with its draft and an error instead of claiming everything was saved.
            val id = state.value.detailId
            if (productIds != null && id != null) {
                mutable.update { it.copy(detail = null, detailLoading = true, detailError = null) }
                fetchDetail(id, ++detailRevision)
            }
            mutable.update { it.copy(mutationError = problem) }
        } finally { mutable.update { it.copy(busy = false) }; mutationLock.unlock() }
    }
    suspend fun delete() {
        if (!mutationLock.tryLock()) return
        try {
            val room = state.value.detail ?: return
            mutable.update { it.copy(busy = true, mutationError = null) }
            source.delete(user, room.id)
            detailRevision++
            mutable.update { it.copy(stage = RoomsStage.List, detail = null, detailId = null, notice = "Cômodo excluído.") }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) { mutable.update { it.copy(mutationError = roomsProblem(error)) }
        } finally { mutable.update { it.copy(busy = false) }; mutationLock.unlock() }
    }
    fun clearMutationError() { mutable.update { it.copy(mutationError = null) } }
    fun notice(message: String?) { mutable.update { it.copy(notice = message) } }
}

internal fun roomsProblem(error: Exception): CatalogProblem {
    if (error !is RoomRejected) return catalogProblem(error)
    val message = when (error.code) {
        "comodo_ja_existe" -> "Já existe um cômodo com esse nome. Confira a lista antes de cadastrar novamente."
        "produto_em_outro_comodo" -> "Um dos produtos já está em outro cômodo. Remova o vínculo anterior antes de continuar."
        "produto_fora_da_estante" -> "A seleção contém produtos que não estão mais na sua estante. Atualize a lista e selecione novamente."
        "comodo_produtos_insuficientes" -> "Selecione pelo menos 2 produtos para criar o cômodo."
        "comodo_produtos_demais" -> "Um cômodo pode ter no máximo 20 produtos."
        "comodo_nao_encontrado" -> "Este cômodo não está mais disponível para sua conta."
        else -> "Não foi possível confirmar a operação. Confira os dados e tente novamente."
    }
    val kind = when (error.status) { 401 -> GenericErrorKind.SessionExpired; 403 -> GenericErrorKind.AccessDenied
        404 -> GenericErrorKind.ContentUnavailable; in 500..599 -> GenericErrorKind.ServiceUnavailable; else -> GenericErrorKind.Unknown }
    return CatalogProblem(kind, message)
}
