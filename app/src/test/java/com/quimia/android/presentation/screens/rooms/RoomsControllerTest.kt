package com.quimia.android.presentation.screens.rooms

import com.quimia.android.data.catalog.*
import com.quimia.android.data.rooms.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class RoomsControllerTest {
    private val products = listOf(ApiCatalogProduct(7, "Produto A", "outro"), ApiCatalogProduct(8, "Produto B", "outro"))
    private fun room(ids: List<Int> = listOf(7, 8)) = ApiRoom(4, "Banheiro", "toilet",
        RoomAssessment("nao_avaliado", ids.size, 0, 0, null, emptyList()), products.filter { it.id in ids })
    private fun page(items: List<ApiRoomSummary> = emptyList(), number: Int = 0, total: Int = items.size) =
        CatalogPage(items, number, 20, total.toLong(), if (total == 0) 0 else (total - 1) / 20 + 1)
    private inner class Source : RoomsDataSource {
        var listCall: suspend (String, Int) -> CatalogPage<ApiRoomSummary> = { _, _ -> page() }
        var createCall: suspend () -> ApiRoom = { room() }
        var detailCall: suspend () -> ApiRoom = { room() }
        var removeCall: suspend () -> Unit = {}
        var addCall: suspend () -> ApiRoom = { room() }
        var deleteCall: suspend () -> Unit = {}
        var editCall: suspend (String, String) -> ApiRoom = { name, icon -> room().copy(nome = name, icone = icon) }
        var shelfProducts = products
        val users = mutableListOf<String>()
        override suspend fun list(user: String, query: String, page: Int): CatalogPage<ApiRoomSummary> { users += user; return listCall(query, page) }
        override suspend fun shelf(user: String) = shelfProducts
        override suspend fun detail(user: String, id: Int) = detailCall()
        override suspend fun create(user: String, name: String, icon: String, ids: List<Int>) = createCall()
        override suspend fun add(user: String, id: Int, ids: List<Int>) = addCall()
        override suspend fun remove(user: String, id: Int, productId: Int) = removeCall()
        override suspend fun edit(user: String, id: Int, name: String, icon: String) = editCall(name, icon)
        override suspend fun delete(user: String, id: Int) = deleteCall()
    }
    @Test fun `edit and delete reflect server confirmation and retain detail on failure`() = runBlocking {
        val source = Source(); val controller = RoomsController(source, "user")
        controller.open(4)
        source.editCall = { _, _ -> throw IOException() }
        controller.edit("Cozinha", RoomIcon.Refrigerator)
        assertEquals("Banheiro", controller.state.value.detail?.nome)
        assertNotNull(controller.state.value.mutationError)
        source.editCall = { name, icon -> room().copy(nome = name, icone = icon) }
        controller.edit("  Cozinha  ", RoomIcon.Refrigerator)
        assertEquals("Cozinha", controller.state.value.detail?.nome)
        source.deleteCall = { throw IOException() }
        controller.delete()
        assertEquals(RoomsStage.Detail, controller.state.value.stage)
        assertNotNull(controller.state.value.mutationError)
        source.deleteCall = {}
        controller.delete()
        assertEquals(RoomsStage.List, controller.state.value.stage)
        assertNull(controller.state.value.detail)
    }
    @Test fun `draft follows API minimum maximum ownership and name rules`() {
        assertNotNull(apiRoomValidation("Banheiro", RoomIcon.Toilet, listOf(7), listOf(7, 8)))
        assertNotNull(apiRoomValidation("Banheiro", RoomIcon.Toilet, (1..21).toList(), (1..21).toList()))
        assertNotNull(apiRoomValidation("Banheiro", RoomIcon.Toilet, listOf(7, 99), listOf(7, 8)))
        assertNull(apiRoomValidation("a".repeat(100), RoomIcon.Toilet, listOf(7, 8), listOf(7, 8)))
        assertNotNull(apiRoomValidation("a".repeat(101), RoomIcon.Toilet, listOf(7, 8), listOf(7, 8)))
        assertEquals("Banheiro social", normalizeRoomName("  Banheiro  social  "))
    }
    @Test fun `offline and successful empty lists are different and search ignores late response`() = runBlocking {
        val source = Source(); val controller = RoomsController(source, "user")
        source.listCall = { _, _ -> throw IOException() }; controller.loadList()
        assertEquals(GenericErrorKind.Offline, controller.state.value.error?.kind)
        source.listCall = { _, _ -> page() }; controller.loadList(); assertNull(controller.state.value.error)
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<CatalogPage<ApiRoomSummary>>()
        source.listCall = { query, _ -> if (query == "old") { entered.complete(Unit); answer.await() } else page() }
        controller.setQuery("old"); val job = launch { controller.loadList() }; entered.await()
        controller.setQuery("new"); controller.loadList(); answer.complete(page(listOf(ApiRoomSummary(4,"Banheiro","toilet",2,"nao_avaliado")))); job.join()
        assertTrue(controller.state.value.rooms.isEmpty()); assertEquals("new", controller.state.value.query)
    }
    @Test fun `creation waits for server and blocks duplicate submission`() = runBlocking {
        val source = Source(); val controller = RoomsController(source, "user"); controller.prepareCreate()
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<ApiRoom>(); var calls = 0
        source.createCall = { calls++; entered.complete(Unit); answer.await() }
        val job = launch { controller.create("Banheiro", RoomIcon.Toilet, listOf(7, 8)) }; entered.await()
        assertEquals(RoomsStage.Saving, controller.state.value.stage); assertNull(controller.state.value.detail)
        controller.create("Banheiro", RoomIcon.Toilet, listOf(7, 8)); assertEquals(1, calls)
        answer.complete(room()); job.join()
        assertEquals(RoomsStage.Success, controller.state.value.stage); assertEquals(setOf(7, 8), controller.state.value.detail?.produtos?.map { it.id }?.toSet())
    }
    @Test fun `conflict never fabricates success and revising does not resend POST`() = runBlocking {
        val source = Source().apply { createCall = { throw RoomRejected("comodo_ja_existe", 409) } }
        val controller = RoomsController(source, "user"); controller.prepareCreate(); controller.create("Banheiro", RoomIcon.Toilet, listOf(7, 8))
        assertEquals(RoomsStage.Error, controller.state.value.stage); assertNull(controller.state.value.detail)
        assertTrue(controller.state.value.mutationError?.message.orEmpty().contains("Já existe"))
        controller.reviseCreate(); assertEquals(RoomsStage.Create, controller.state.value.stage)
    }
    @Test fun `removal keeps old data on failure and refreshes server assessment only after confirmation`() = runBlocking {
        val source = Source(); val controller = RoomsController(source, "user"); controller.open(4)
        source.removeCall = { throw IOException() }; controller.remove(7)
        assertEquals(2, controller.state.value.detail?.produtos?.size); assertNotNull(controller.state.value.mutationError)
        source.removeCall = {}; source.detailCall = { room(listOf(8)) }; controller.remove(7)
        assertEquals(listOf(8), controller.state.value.detail?.produtos?.map { it.id })
        assertEquals("nao_avaliado", controller.state.value.detail?.avaliacao?.status)
        assertTrue(controller.state.value.notice.orEmpty().contains("continua na sua estante"))
    }
    @Test fun `confirmed removal with failed refresh hides stale safety assessment`() = runBlocking {
        val source = Source(); val controller = RoomsController(source, "user"); controller.open(4)
        source.detailCall = { throw IOException() }; controller.remove(7)
        assertNull(controller.state.value.detail); assertNotNull(controller.state.value.detailError)
        assertFalse(controller.state.value.busy)
    }
    @Test fun `adding accepts one product and uses returned server assessment`() = runBlocking {
        val source = Source().apply { detailCall = { room(listOf(7)) } }
        val controller = RoomsController(source, "user"); controller.open(4); controller.prepareAdd(); controller.add(listOf(8))
        assertEquals(RoomsStage.Detail, controller.state.value.stage); assertEquals(2, controller.state.value.detail?.produtos?.size)
        assertEquals("nao_avaliado", controller.state.value.detail?.avaliacao?.status)
    }
    @Test fun `leaving failed addition reloads instead of showing stale safety`() = runBlocking {
        val source = Source().apply { detailCall = { room(listOf(7)) }; addCall = { throw IOException() } }
        val controller = RoomsController(source, "user"); controller.open(4); controller.prepareAdd(); controller.add(listOf(8))
        assertNotNull(controller.state.value.mutationError)
        source.detailCall = { throw IOException() }; controller.cancelAdd()
        assertEquals(RoomsStage.Detail, controller.state.value.stage)
        assertNull(controller.state.value.detail); assertNotNull(controller.state.value.detailError)
    }
    @Test fun `unknown icons and assessments are neutral and account controllers do not share data`() = runBlocking {
        assertNull(apiRoomIcon("unknown")); assertEquals(RoomSafety.NotEvaluated, apiRoomSafety("atencao"))
        assertEquals(RoomSafety.NotEvaluated, apiRoomSafety("unknown")); assertEquals(RoomIcon.Toilet, apiRoomIcon(RoomIcon.Toilet.apiCode))
        assertFalse(room().copy(icone = "unknown").uiRoom().knownIcon)
        val source = Source(); RoomsController(source, "one").loadList(); RoomsController(source, "two").loadList()
        assertEquals(listOf("one", "two"), source.users)
    }
    @Test fun `edit saves identity and selected products using confirmed server assessment`() = runBlocking {
        val extra = ApiCatalogProduct(9, "Produto C", "outro")
        var server = room()
        val calls = mutableListOf<String>()
        val source = Source().apply {
            shelfProducts = products + extra
            detailCall = { server }
            editCall = { name, icon -> calls += "edit"; server = server.copy(nome = name, icone = icon); server }
            removeCall = { calls += "remove"; server = server.copy(produtos = server.produtos.filterNot { it.id == 8 },
                avaliacao = RoomAssessment("nao_avaliado", 1, 0, 0, null, emptyList())) }
            addCall = { calls += "add"; server = server.copy(produtos = server.produtos + extra,
                avaliacao = RoomAssessment("nao_avaliado", 2, 0, 0, null, emptyList())); server }
        }
        val controller = RoomsController(source, "user")
        controller.open(4); controller.prepareEdit()
        assertEquals(RoomsStage.Editing, controller.state.value.stage)
        controller.edit("Cozinha", RoomIcon.Refrigerator, listOf(7, 9))
        assertEquals(listOf("edit", "remove", "add"), calls)
        assertEquals("Cozinha", controller.state.value.detail?.nome)
        assertEquals(setOf(7, 9), controller.state.value.detail?.produtos?.map { it.id }?.toSet())
        assertEquals(RoomsStage.Detail, controller.state.value.stage)
        assertNull(controller.state.value.mutationError)
    }
    @Test fun `partial edit stays in form and retries without repeating confirmed removals`() = runBlocking {
        val extra = ApiCatalogProduct(9, "Produto C", "outro")
        var server = room(); var removals = 0
        val source = Source().apply {
            shelfProducts = products + extra
            detailCall = { server }
            removeCall = { removals++; server = server.copy(produtos = server.produtos.filterNot { it.id == 8 },
                avaliacao = RoomAssessment("nao_avaliado", 1, 0, 0, null, emptyList())) }
            addCall = { throw IOException() }
        }
        val controller = RoomsController(source, "user")
        controller.open(4); controller.prepareEdit()
        controller.edit("Banheiro", RoomIcon.Toilet, listOf(7, 9))
        assertEquals(RoomsStage.Editing, controller.state.value.stage)
        assertNotNull(controller.state.value.mutationError)
        assertEquals(listOf(7), controller.state.value.detail?.produtos?.map { it.id })
        source.addCall = { server = server.copy(produtos = server.produtos + extra,
            avaliacao = RoomAssessment("nao_avaliado", 2, 0, 0, null, emptyList())); server }
        controller.edit("Banheiro", RoomIcon.Toilet, listOf(7, 9))
        assertEquals(1, removals)
        assertEquals(RoomsStage.Detail, controller.state.value.stage)
        assertNull(controller.state.value.mutationError)
    }
    @Test fun `edit rejects products outside shelf before any API mutation`() = runBlocking {
        var calls = 0
        val source = Source().apply { editCall = { _, _ -> calls++; room() } }
        val controller = RoomsController(source, "user")
        controller.open(4); controller.prepareEdit()
        controller.edit("Cozinha", RoomIcon.Refrigerator, listOf(7, 999))
        assertEquals(0, calls)
        assertEquals(RoomsStage.Editing, controller.state.value.stage)
        assertEquals(setOf(7, 8), controller.state.value.detail?.produtos?.map { it.id }?.toSet())
        assertNotNull(controller.state.value.notice)
    }
}
