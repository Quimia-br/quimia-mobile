package com.quimia.android.presentation.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quimia.android.data.project.*
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.rooms.*
import com.quimia.android.presentation.screens.shelf.ShelfSubstance
import com.quimia.android.presentation.screens.proximity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

data class ApiChatEntry(val text: String, val fromAssistant: Boolean)

data class ProjectState(
    val profile: ApiUser? = null, val products: List<CatalogProduct> = emptyList(),
    val shelf: List<CatalogProduct> = emptyList(), val substances: List<ShelfSubstance> = emptyList(),
    val rooms: List<QuimiaRoom> = emptyList(), val points: List<ApiPoint> = emptyList(),
    val mapPoints: List<NearbyPoint> = emptyList(), val address: ApiAddress? = null,
    val mixtures: List<ApiMixture> = emptyList(), val loading: Set<String> = emptySet(),
    val errors: Map<String, String> = emptyMap(), val pendingWrites: Int = 0,
    val loaded: Set<String> = emptySet(),
    val chat: List<ApiChatEntry> = emptyList(), val chatSessionId: String? = null,
    val chatBusy: Boolean = false, val chatError: String? = null,
)

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val client = ProjectClient.get(application)
    private val api = client.api
    private val mutableState = MutableStateFlow(ProjectState())
    val state = mutableState.asStateFlow()
    val message = MutableStateFlow<String?>(null)
    private val writes = Mutex()
    private var owner: String? = null

    init {
        viewModelScope.launch {
            client.sessions.session.map { it?.user?.id }.distinctUntilChanged().collectLatest { id ->
                owner = id; mutableState.value = ProjectState()
                if (id != null) supervisorScope {
                    listOf("profile", "catalog", "shelf", "substances", "rooms", "points", "address", "history").forEach { section ->
                        launch { load(section) }
                    }
                }
            }
        }
    }

    fun retry(section: String) { viewModelScope.launch { load(section) } }

    fun sendChat(text: String) {
        if (state.value.chatBusy) return
        val id = owner ?: return
        val question = text.trim()
        if (question.length !in 1..2000) {
            mutableState.update { it.copy(chatError = "A mensagem deve conter entre 1 e 2000 caracteres.") }
            return
        }
        val conversation = state.value.chatSessionId
        mutableState.update { it.copy(chatBusy = true, chatError = null) }
        viewModelScope.launch {
            try {
                val reply = withContext(Dispatchers.IO) {
                    api.chat(buildJsonObject { put("texto", question); conversation?.let { put("sessaoId", it) } })
                }
                if (reply.texto.isBlank() || !reply.sessaoId.matches(Regex("[A-Za-z0-9_-]{1,64}")))
                    throw ApiContractException("POST /api/v1/chat/mensagens retornou texto vazio ou sessaoId inválido.")
                checked(id) { it.copy(chatSessionId = reply.sessaoId,
                    chat = it.chat + ApiChatEntry(question, false) + ApiChatEntry(reply.texto, true)) }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { checked(id) { it.copy(chatError = apiErrorMessage(error)) } }
            finally { if (owner == id) mutableState.update { it.copy(chatBusy = false) } }
        }
    }

    suspend fun lookupBarcode(barcode: String): CatalogProduct = withContext(Dispatchers.IO) {
        val id = owner ?: throw ApiContractException("Entre novamente para consultar produtos.")
        val real = api.barcode(apiBody("codBarras" to barcode)).toProduct()
        checked(id) { it.copy(products = (it.products + real).distinctBy { product -> product.id }) }
        real
    }

    fun openProductRoom(productId: String, onFound: (String) -> Unit, onMissing: () -> Unit) {
        val id = owner ?: return
        viewModelScope.launch {
            try {
                val real = withContext(Dispatchers.IO) { api.room(api.productRoom(productId.toInt()).id) }
                val room = real.toRoom()
                checked(id) { it.copy(rooms = it.rooms.upsertRoom(room),
                    products = (it.products + real.produtos.map { product -> product.toProduct() }).distinctBy { product -> product.id }) }
                onFound(room.id)
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                checked(id) { it }
                if (isApiProblem(error, 404, "produto_sem_comodo")) onMissing() else message.value = apiErrorMessage(error)
            }
        }
    }

    private suspend fun load(section: String) {
        val id = owner ?: return
        mutableState.update { it.copy(loading = it.loading + section, errors = it.errors - section) }
        try {
            withContext(Dispatchers.IO) {
                when (section) {
                    "profile" -> api.me().let { value -> checked(id) { it.copy(profile = value) } }
                    "catalog" -> readApiPages { api.products(it) }.map { it.toProduct() }.let { value -> checked(id) { it.copy(products = value) } }
                    "shelf" -> readApiPages { api.shelf(it) }.map { it.toProduct() }.let { value -> checked(id) { it.copy(shelf = value) } }
                    "substances" -> api.substances().itens.map { it.toSubstance() }.let { value -> checked(id) { it.copy(substances = value) } }
                    "rooms" -> {
                        val details = readApiPages { api.rooms(it) }.map { api.room(it.id) }
                        checked(id) { it.copy(rooms = details.map { it.toRoom() },
                            products = (it.products + details.flatMap { room -> room.produtos.map { product -> product.toProduct() } }).distinctBy { it.id }) }
                    }
                    "points" -> {
                        val points = readApiPages { api.points(it) }
                        checked(id) { it.copy(points = points) }
                        checked(id) { it.copy(mapPoints = emptyList()) }
                    }
                    "address" -> {
                        val value = try { api.address() } catch (error: Exception) {
                            if (isApiProblem(error, 404, "not_found")) null else throw error
                        }
                        checked(id) { it.copy(address = value) }
                    }
                    "history" -> readApiPages { api.mixtures(it) }.let { value -> checked(id) { it.copy(mixtures = value) } }
                }
                checked(id) { it.copy(loaded = it.loaded + section) }
            }
        } catch (error: CancellationException) { throw error }
        catch (error: Exception) { checked(id) { it.copy(errors = it.errors + (section to apiErrorMessage(error))) } }
        finally { if (owner == id) mutableState.update { it.copy(loading = it.loading - section) } }
    }

    private fun checked(id: String, transform: (ProjectState) -> ProjectState) {
        if (owner != id || client.sessions.session.value?.user?.id != id) throw CancellationException("Sessão alterada")
        mutableState.update(transform)
    }

    private fun requireSnapshot(section: String) {
        val current = state.value
        if (section !in current.loaded || section in current.loading || section in current.errors)
            throw ApiContractException("Os dados de $section ainda não foram confirmados pela API. Recarregue a seção antes de alterar informações.")
    }

    private suspend fun <T> write(block: suspend (String) -> T): T = writes.withLock {
        val id = owner ?: throw IllegalStateException("Entre novamente")
        mutableState.update { it.copy(pendingWrites = it.pendingWrites + 1) }
        try { withContext(Dispatchers.IO) { block(id).also { checked(id) { it } } } }
        finally { if (owner == id) mutableState.update { it.copy(pendingWrites = (it.pendingWrites - 1).coerceAtLeast(0)) } }
    }

    suspend fun saveProducts(desiredIds: List<String>) = write { id ->
        requireSnapshot("shelf")
        val previous = state.value.shelf.map { it.id }
        try {
            val added = desiredIds - previous.toSet()
            if (added.isNotEmpty()) api.saveProducts(apiIds(added)).requireSaved(added)
            (previous - desiredIds.toSet()).forEach { api.removeProduct(it.toInt()) }
        } finally {
            val real = readApiPages { api.shelf(it) }.map { it.toProduct() }
            checked(id) { it.copy(shelf = real) }
            load("substances")
            load("rooms")
        }
    }

    suspend fun createRoom(draft: QuimiaRoom): QuimiaRoom = write { id ->
        requireSnapshot("shelf")
        val body = buildJsonObject { put("nome", draft.name); put("icone", draft.icon.apiCode()); put("produtoIds", apiIds(draft.productIds).getValue("produtoIds")) }
        val real = api.createRoom(body).toRoom()
        checked(id) { it.copy(rooms = it.rooms.upsertRoom(real)) }
        real
    }

    suspend fun removeRoomProduct(roomId: String, productId: String) = write { id ->
        requireSnapshot("rooms")
        api.removeRoomProduct(roomId.toInt(), productId.toInt())
        val real = api.room(roomId.toInt()).toRoom()
        checked(id) { it.copy(rooms = it.rooms.upsertRoom(real)) }
    }

    suspend fun updateProfileName(name: String) = write { id ->
        val real = api.updateProfile(apiBody("nome" to name))
        checked(id) { it.copy(profile = real) }
    }

    suspend fun updateAddressValue(address: ApiAddress) = write { id ->
        requireSnapshot("address")
        val real = api.updateAddress(address)
        checked(id) { it.copy(address = real) }
    }

    suspend fun mix(first: String, second: String): ApiMixture = write { id ->
        requireSnapshot("shelf")
        if (first == second || listOf(first, second).any { productId -> state.value.shelf.none { it.id == productId } })
            throw ApiContractException("Escolha dois produtos diferentes que a API confirmou na sua estante.")
        val real = api.mix(buildJsonObject { put("produtoIdA", first.toInt()); put("produtoIdB", second.toInt()) })
        checked(id) { it.copy(mixtures = listOf(real) + it.mixtures.filterNot { previous -> previous.id == real.id }) }
        real
    }

    suspend fun productSafety(productId: String): String {
        val info = api.firstAid(productId.toInt())
        if (!info.encontrada) return "A API não disponibiliza primeiros socorros para este produto."
        return (info.porExposicao.map { listOfNotNull(it.rotaExposicao, it.descricao, it.sintomas, it.tratamentoEspecial,
            if (it.atencaoMedicaImediata) "Atenção médica imediata indicada pela FDS." else null).joinToString("\n") }
            + info.orientacoesGerais + info.notasAoMedico).joinToString("\n\n")
            .ifBlank { "A API informou FDS encontrada, mas não retornou orientações de primeiros socorros." }
    }
}
