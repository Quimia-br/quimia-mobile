package com.quimia.android.data.catalog

import com.quimia.android.data.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CatalogRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: CatalogRepository
    private val profile = """{"id":"u1","nome":"Ana","email":"ana@example.com"}"""
    private val product = """{"id":7,"nome":"Produto real","tipo":"limpeza_geral","codBarras":"0123456789012"}"""
    private val now = 1_000_000L
    private class Store : SessionStore {
        var value: StoredSession? = StoredSession("access", "refresh", 2_000_000L, "u1")
        override fun read() = value
        override fun write(session: StoredSession) { value = session }
        override fun clear() { value = null }
    }
    private fun response(body: String, status: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    }
    private fun page(items: String = product, number: Int = 0, size: Int = 20, total: Int = 1) =
        """{"itens":[$items],"pagina":$number,"tamanho":$size,"totalItens":$total,"totalPaginas":${if(total == 0) 0 else (total - 1) / size + 1}}"""
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val base = server.url("/").toString()
        val sessions = AuthRepository(AuthApiFactory.create(base), Store()) { now }
        response(profile); sessions.restore(); server.takeRequest()
        repository = CatalogRepository(AuthApiFactory.create(base, CatalogApi::class.java), sessions)
        Unit
    }
    @After fun teardown() { server.shutdown() }

    @Test fun `catalog uses real JSON search pagination and authenticated details`() = runBlocking {
        response(page()); val result = repository.products("u1", "  multi uso  ", 0, 20)
        val request = server.takeRequest()
        assertEquals("/api/v1/produtos", request.requestUrl?.encodedPath)
        assertEquals("multi uso", request.requestUrl?.queryParameter("q"))
        assertEquals("0", request.requestUrl?.queryParameter("page"))
        assertEquals("20", request.requestUrl?.queryParameter("size"))
        assertEquals("Bearer access", request.getHeader("Authorization"))
        assertNull(request.getHeader("Cookie"))
        assertNull(result.itens.single().fotoUrl)
        response(product); assertEquals(7, repository.detail("u1", 7).id)
        assertEquals("/api/v1/produtos/7", server.takeRequest().path)
    }
    @Test fun `saved products load all pages before returning IDs`() = runBlocking {
        response(page(size = 50, total = 51))
        response(page(product.replace("\"id\":7", "\"id\":8"), number = 1, size = 50, total = 51))
        assertEquals(setOf(7, 8), repository.savedIds("u1"))
        assertEquals("/api/v1/estante/produtos?page=0&size=50", server.takeRequest().path)
        assertEquals("/api/v1/estante/produtos?page=1&size=50", server.takeRequest().path)
    }
    @Test fun `save distinguishes added already saved and not found`() = runBlocking {
        response("""{"itens":[{"produtoId":7,"nome":"Produto real","status":"adicionado"},{"produtoId":8,"nome":"Outro","status":"ja_na_estante"},{"produtoId":9,"nome":null,"status":"nao_encontrado"}]}""")
        val result = repository.save("u1", listOf(7, 8, 9, 7))
        assertEquals(listOf("adicionado", "ja_na_estante", "nao_encontrado"), result.map { it.status })
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/estante/produtos", request.path)
        assertEquals("""{"produtoIds":[7,8,9]}""", request.body.readUtf8())
    }
    @Test fun `API product with null type does not discard the catalog page or shelf`() = runBlocking {
        val withoutType = """{"id":113,"nome":"Alvejante Profissional","tipo":null,"descricao":null,"codBarras":"0909934821609","fotoUrl":null,"marca":{"id":54,"nome":"Dias","logoUrl":null}}"""
        response(page(items = "$product,$withoutType", total = 2))
        val result = repository.products("u1", "", 0, 20)
        assertEquals(listOf(7, 113), result.itens.map { it.id })
        assertNull(result.itens.last().tipo)
        response(page(items = "$product,$withoutType", size = 50, total = 2))
        assertEquals(setOf(7, 113), repository.savedIds("u1"))
    }

    @Test fun `missing type also works in product details and barcode lookup`() = runBlocking {
        val withoutType = """{"id":113,"nome":"Alvejante Profissional","tipo":null,"codBarras":"0909934821609"}"""
        response(withoutType)
        assertEquals(113, repository.detail("u1", 113).id)
        response(withoutType)
        assertEquals(113, repository.barcode("u1", "0909934821609").id)
    }

    @Test fun `malformed success cannot confirm saving`() = runBlocking {
        response("""{"itens":[{"produtoId":7,"nome":"Produto real","status":"ok"}]}""")
        assertTrue(runCatching { repository.save("u1", listOf(7)) }.exceptionOrNull() is CatalogContractException)
        response("""{"itens":[]}""")
        assertTrue(runCatching { repository.save("u1", listOf(7)) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `barcode accepts equivalent UPC and rejects unrelated product`() = runBlocking {
        response(product)
        assertEquals(7, repository.barcode("u1", "123456789012").id)
        val request = server.takeRequest()
        assertEquals("/api/v1/produtos/busca-por-codigo-barras", request.path)
        assertEquals("""{"codBarras":"123456789012"}""", request.body.readUtf8())
        response(product)
        assertTrue(runCatching { repository.barcode("u1", "99999999") }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `401 refreshes once and uses renewed token`() = runBlocking {
        response("{}", 401)
        response("""{"accessToken":"new-access","refreshToken":"new-refresh","tokenType":"Bearer","expiresIn":900,"user":$profile}""")
        response(page())
        repository.products("u1", "", 0, 20)
        assertEquals("Bearer access", server.takeRequest().getHeader("Authorization"))
        assertEquals("/api/v1/auth/mobile/refresh", server.takeRequest().path)
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
    }
    @Test fun `extracted barcode is sent as string with all leading zeros`() = runBlocking {
        response(product.replace("0123456789012", "0000123456789"))
        repository.barcode("u1", "0000123456789")
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/produtos/busca-por-codigo-barras", request.path)
        assertEquals("""{"codBarras":"0000123456789"}""", request.body.readUtf8())
    }
    @Test fun `wrong account sends no request and expired refresh signs out`() = runBlocking {
        assertTrue(runCatching { repository.products("other", "", 0, 20) }.isFailure)
        assertEquals(1, server.requestCount)
        response("{}", 401); response("{}", 401)
        assertTrue(runCatching { repository.products("u1", "", 0, 20) }.exceptionOrNull() is CatalogSessionExpired)
        assertEquals(3, server.requestCount)
    }
}
