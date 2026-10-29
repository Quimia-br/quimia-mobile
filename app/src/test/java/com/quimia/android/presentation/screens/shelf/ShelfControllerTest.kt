package com.quimia.android.presentation.screens.shelf

import com.quimia.android.data.catalog.*
import com.quimia.android.data.shelf.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ShelfControllerTest {
    private val product = ApiCatalogProduct(7, "Produto real", "limpeza_geral")
    private fun page(items: List<ApiCatalogProduct> = listOf(product), number: Int = 0, total: Int = items.size) =
        CatalogPage(items, number, 20, total.toLong(), if (total == 0) 0 else (total - 1) / 20 + 1)
    private inner class Source : ShelfDataSource {
        var productsCall: suspend (String, Int) -> CatalogPage<ApiCatalogProduct> = { _, _ -> page() }
        var substancesCall: suspend () -> List<ShelfApiSubstance> = { emptyList() }
        var aidCall: suspend (Int) -> ShelfFirstAid = { ShelfFirstAid(it, false, emptyList(), emptyList(), emptyList()) }
        var removeCall: suspend () -> Unit = {}
        var roomCall: suspend () -> ShelfRoom? = { null }
        val users = mutableListOf<String>()
        override suspend fun products(user: String, query: String, order: String?, page: Int): CatalogPage<ApiCatalogProduct> {
            users += user; return productsCall(query, page)
        }
        override suspend fun substances(user: String, query: String, order: String?) = substancesCall()
        override suspend fun firstAid(user: String, id: Int) = aidCall(id)
        override suspend fun summary(user: String, id: Int) = ShelfSummary(id)
        override suspend fun room(user: String, id: Int) = roomCall()
        override suspend fun remove(user: String, id: Int) = removeCall()
        override suspend fun history(user: String, id: Int, page: Int) = CatalogPage<ShelfMixture>(emptyList(), page, 20, 0, 0)
    }
    @Test fun `empty loading and failed requests have distinct states`() = runBlocking {
        val source = Source(); val controller = ShelfController(source, "user")
        assertTrue(controller.state.value.loading)
        source.productsCall = { _, _ -> throw IOException() }; controller.load()
        assertEquals(GenericErrorKind.Offline, controller.state.value.error?.kind)
        source.productsCall = { _, _ -> page(emptyList()) }; controller.load()
        assertFalse(controller.state.value.loading); assertNull(controller.state.value.error)
        assertTrue(controller.state.value.products.isEmpty())
    }
    @Test fun `switching tab ignores a late product search and unsupported sorts never fake order`() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<CatalogPage<ApiCatalogProduct>>()
        val source = Source().apply { productsCall = { _, _ -> entered.complete(Unit); answer.await() } }
        val controller = ShelfController(source, "user")
        val job = launch { controller.load() }; entered.await()
        controller.setTab(true); controller.load(); answer.complete(page()); job.join()
        assertTrue(controller.state.value.substancesTab); assertTrue(controller.state.value.products.isEmpty())
        controller.setSort(ShelfSort.Recent)
        assertEquals("alfabetica", controller.state.value.order); assertNotNull(controller.state.value.notice)
    }
    @Test fun `pagination retains data and retries the same page after timeout`() = runBlocking {
        val source = Source().apply { productsCall = { _, _ -> page(total = 21) } }
        val controller = ShelfController(source, "user"); controller.load()
        source.productsCall = { _, _ -> throw SocketTimeoutException() }; controller.loadMore()
        assertEquals(listOf(product), controller.state.value.products); assertEquals(1, controller.state.value.nextPage)
        source.productsCall = { _, number -> page(listOf(product, product.copy(id = 8)), number, 21) }; controller.loadMore()
        assertEquals(listOf(7, 8), controller.state.value.products.map { it.id }); assertNull(controller.state.value.nextPage)
    }
    @Test fun `failed removal keeps product and only confirmed deletion changes list`() = runBlocking {
        val source = Source(); val controller = ShelfController(source, "user"); controller.load()
        source.removeCall = { throw IOException() }; controller.remove(7)
        assertEquals(listOf(product), controller.state.value.products); assertNull(controller.state.value.removedId)
        assertNotNull(controller.state.value.removeError)
        source.removeCall = {}; source.productsCall = { _, _ -> page(emptyList()) }; controller.remove(7)
        assertEquals(7, controller.state.value.removedId); assertTrue(controller.state.value.products.isEmpty())
    }
    @Test fun `double removal is blocked and cannot show success before response`() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<Unit>(); var calls = 0
        val source = Source().apply { removeCall = { calls++; entered.complete(Unit); answer.await() } }
        val controller = ShelfController(source, "user"); controller.load()
        val job = launch { controller.remove(7) }; entered.await(); controller.remove(7)
        assertEquals(1, calls); assertNull(controller.state.value.removedId); assertEquals(listOf(product), controller.state.value.products)
        source.productsCall = { _, _ -> page(emptyList()) }; answer.complete(Unit); job.join()
        assertEquals(7, controller.state.value.removedId)
    }
    @Test fun `FDS absence never fabricates safety instructions and cached protection is reusable`() = runBlocking {
        val source = Source(); var calls = 0
        source.aidCall = { calls++; ShelfFirstAid(it, false, emptyList(), emptyList(), emptyList()) }
        val controller = ShelfController(source, "user"); controller.protect(7); controller.protect(7)
        assertEquals(1, calls)
        assertEquals("Primeiros socorros não informados para este produto.", firstAidText(requireNotNull(controller.state.value.protection[7]?.data)))
        val instructions = ShelfFirstAid(7, true, listOf(ShelfExposure("olhos", "Orientação da FDS", atencaoMedicaImediata = true)),
            listOf("Texto geral da FDS"), listOf("Nota original"))
        assertTrue(firstAidText(instructions).contains("Orientação da FDS"))
        assertTrue(firstAidText(instructions).contains("Nota original"))
    }
    @Test fun `room failure is not interpreted as no room and close ignores late data`() = runBlocking {
        val source = Source().apply { roomCall = { throw IOException() } }; val controller = ShelfController(source, "user")
        controller.showRoom(7); assertNotNull(controller.state.value.room.error)
        source.roomCall = { null }; controller.showRoom(7)
        assertNull(controller.state.value.room.error); assertNull(controller.state.value.room.data)
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<ShelfRoom?>()
        source.roomCall = { entered.complete(Unit); answer.await() }
        val job = launch { controller.showRoom(7) }; entered.await(); controller.closeRoom()
        answer.complete(ShelfRoom(1, "Banheiro", "banheiro", 2, "nao_avaliado")); job.join()
        assertNull(controller.state.value.roomId)
    }
    @Test fun `substances preserve real associations and users do not share lists`() = runBlocking {
        val source = Source(); val one = ShelfController(source, "one"); val two = ShelfController(source, "two")
        one.load(); two.load(); assertEquals(listOf("one", "two"), source.users)
        val model = ShelfApiSubstance(1, "Substância", null, null, listOf("classe"), listOf(ShelfProductReference(7, "Produto real"))).shelfSubstance()
        assertEquals(listOf("Produto real"), model.products); assertEquals("Descrição não informada.", model.description)
        assertEquals("Não avaliado", shelfMixtureLabel("nao_avaliado"))
        assertTrue(shelfMixtureLabel("unknown").contains("não reconhecido"))
    }
}
