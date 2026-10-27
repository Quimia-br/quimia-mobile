package com.quimia.android.presentation.screens.catalog

import com.quimia.android.data.catalog.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class CatalogControllerTest {
    private val first = ApiCatalogProduct(1, "Produto um", "limpeza_geral")
    private val second = ApiCatalogProduct(2, "Produto dois", "desinfetante")
    private fun page(items: List<ApiCatalogProduct>, page: Int = 0, total: Int = items.size) =
        CatalogPage(items, page, 20, total.toLong(), if (total == 0) 0 else (total - 1) / 20 + 1)
    private inner class Source : CatalogDataSource {
        var load: suspend (String, Int) -> CatalogPage<ApiCatalogProduct> = { _, _ -> page(listOf(first)) }
        var shelf: suspend () -> Set<Int> = { emptySet() }
        var saving: suspend (List<Int>) -> List<SavedCatalogItem> = { ids -> ids.map { SavedCatalogItem(it, "Produto", "adicionado") } }
        var detailLoad: suspend (Int) -> ApiCatalogProduct = { first.copy(id = it) }
        val users = mutableListOf<String>()
        override suspend fun products(userId: String, query: String, page: Int, size: Int): CatalogPage<ApiCatalogProduct> {
            users += userId; return load(query, page)
        }
        override suspend fun savedIds(userId: String): Set<Int> { users += userId; return shelf() }
        override suspend fun detail(userId: String, id: Int) = detailLoad(id)
        override suspend fun barcode(userId: String, code: String) = first
        override suspend fun save(userId: String, ids: List<Int>) = saving(ids)
    }
    @Test fun `loading empty and offline are different states`() = runBlocking {
        val source = Source()
        val controller = CatalogController(source, "user")
        assertTrue(controller.state.value.loading)
        source.load = { _, _ -> throw IOException() }; controller.search()
        assertEquals(GenericErrorKind.Offline, controller.state.value.error?.kind)
        source.load = { _, _ -> page(emptyList()) }; controller.search()
        assertFalse(controller.state.value.loading); assertNull(controller.state.value.error)
        assertTrue(controller.state.value.products.isEmpty())
    }
    @Test fun `search ignores late response for previous query`() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val old = CompletableDeferred<CatalogPage<ApiCatalogProduct>>()
        val source = Source().apply { load = { query, _ -> if (query == "old") { entered.complete(Unit); old.await() } else page(listOf(second)) } }
        val controller = CatalogController(source, "user")
        controller.setQuery("old"); val job = launch { controller.search() }; entered.await()
        controller.setQuery("new"); controller.search(); old.complete(page(listOf(first))); job.join()
        assertEquals(listOf(second), controller.state.value.products)
        assertEquals("new", controller.state.value.query)
    }
    @Test fun `pagination preserves products on timeout and deduplicates retry`() = runBlocking {
        val source = Source().apply { load = { _, _ -> page(listOf(first), total = 21) } }
        val controller = CatalogController(source, "user"); controller.search()
        source.load = { _, _ -> throw SocketTimeoutException() }; controller.loadMore()
        assertEquals(listOf(first), controller.state.value.products)
        assertEquals(1, controller.state.value.nextPage)
        assertEquals(GenericErrorKind.Timeout, controller.state.value.moreError?.kind)
        source.load = { _, number -> page(listOf(first, second), number, 21) }; controller.loadMore()
        assertEquals(listOf(first, second), controller.state.value.products); assertNull(controller.state.value.nextPage)
    }
    @Test fun `saving waits for shelf and confirms only successful items`() = runBlocking {
        val source = Source(); val controller = CatalogController(source, "user")
        controller.save(listOf(1)); assertTrue(controller.state.value.savedIds.isEmpty())
        controller.loadShelf()
        source.saving = { listOf(SavedCatalogItem(1, "Produto um", "adicionado"), SavedCatalogItem(2, null, "nao_encontrado")) }
        controller.save(listOf(1, 2))
        assertEquals(setOf(1), controller.state.value.savedIds)
        assertFalse(controller.state.value.noticeSuccess)
        assertTrue(controller.state.value.notice.orEmpty().contains("não encontrado"))
    }
    @Test fun `save error never invents confirmation and retry preserves state`() = runBlocking {
        val source = Source(); val controller = CatalogController(source, "user"); controller.loadShelf()
        source.saving = { throw IOException() }; controller.save(listOf(1))
        assertTrue(controller.state.value.savedIds.isEmpty()); assertNull(controller.state.value.notice)
        assertNotNull(controller.state.value.saveError); assertTrue(controller.state.value.savingIds.isEmpty())
        source.saving = { listOf(SavedCatalogItem(1, "Produto um", "ja_na_estante")) }; controller.retrySave()
        assertEquals(setOf(1), controller.state.value.savedIds)
    }
    @Test fun `duplicate save is blocked while request is pending`() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<List<SavedCatalogItem>>()
        var calls = 0
        val source = Source().apply { saving = { calls++; entered.complete(Unit); answer.await() } }
        val controller = CatalogController(source, "user"); controller.loadShelf()
        val job = launch { controller.save(listOf(1)) }; entered.await(); controller.save(listOf(1))
        assertEquals(1, calls); assertEquals(setOf(1), controller.state.value.savingIds)
        answer.complete(listOf(SavedCatalogItem(1, "Produto", "adicionado"))); job.join()
    }
    @Test fun `closing details ignores late response and accounts are isolated`() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val answer = CompletableDeferred<ApiCatalogProduct>()
        val source = Source().apply { detailLoad = { entered.complete(Unit); answer.await() } }
        val controller = CatalogController(source, "user-a")
        val job = launch { controller.openDetail(1) }; entered.await(); controller.closeDetail(); answer.complete(first); job.join()
        assertFalse(controller.state.value.detailOpen); assertNull(controller.state.value.detail)
        controller.search(); CatalogController(source, "user-b").search()
        assertEquals(listOf("user-a", "user-b"), source.users)
    }
    @Test fun `missing data is error and remote products never use sample photos`() = runBlocking {
        val source = Source().apply { load = { _, _ -> page(listOf(first.copy(nome = ""))) } }
        val controller = CatalogController(source, "user"); controller.search()
        assertNotNull(controller.state.value.error)
        val model = first.catalogProduct()
        assertTrue(model.remote); assertEquals(0, model.imageRes); assertNull(model.photoUrl)
        assertEquals("Descrição não informada.", model.description)
        assertNull(catalogImageUrl("file:///private/photo.png"))
        assertNull(catalogImageUrl("https://user:password@example.com/photo.png"))
    }
    @Test fun `barcode decoder reads a photographed style barcode and rotated pixels`() {
        val code = "7891234567895"
        val matrix = MultiFormatWriter().encode(code, BarcodeFormat.EAN_13, 420, 180)
        val pixels = IntArray(matrix.width * matrix.height) { if (matrix[it % matrix.width, it / matrix.width]) -16777216 else -1 }
        assertEquals(code, decodeCatalogPixels(matrix.width, matrix.height, pixels))
        val rotated = IntArray(pixels.size)
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) rotated[x * matrix.height + matrix.height - y - 1] = pixels[y * matrix.width + x]
        assertEquals(code, decodeCatalogPixels(matrix.height, matrix.width, rotated))
        assertNull(decodeCatalogPixels(20, 20, IntArray(400) { -1 }))
    }
}
