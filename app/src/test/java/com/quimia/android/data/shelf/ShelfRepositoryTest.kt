package com.quimia.android.data.shelf

import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ShelfRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ShelfRepository
    private val profile = """{"id":"u1","nome":"Ana","email":"ana@example.com"}"""
    private val product = """{"id":7,"nome":"Produto real","tipo":"limpeza_geral"}"""
    private val aid = """{"produtoId":7,"encontrada":false,"porExposicao":[],"orientacoesGerais":[],"notasAoMedico":[],"versaoFds":null,"dataAtualizacaoFds":null}"""
    private class Store : SessionStore {
        var value: StoredSession? = StoredSession("access", "refresh", 2_000_000L, "u1")
        override fun read() = value
        override fun write(session: StoredSession) { value = session }
        override fun clear() { value = null }
    }
    private fun response(body: String = "", code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val url = server.url("/").toString()
        val sessions = AuthRepository(AuthApiFactory.create(url), Store()) { 1_000_000L }
        response(profile); sessions.restore(); server.takeRequest()
        repository = ShelfRepository(AuthApiFactory.create(url, ShelfApi::class.java), sessions)
        Unit
    }
    @After fun teardown() { server.shutdown() }

    @Test fun `products and substances are scoped to authenticated shelf with real search and order`() = runBlocking {
        response("""{"itens":[$product],"pagina":0,"tamanho":20,"totalItens":1,"totalPaginas":1}""")
        assertEquals(7, repository.products("u1", "  real ", "alfabetica", 0).itens.single().id)
        val products = server.takeRequest()
        assertEquals("/api/v1/estante/produtos", products.requestUrl?.encodedPath)
        assertEquals("real", products.requestUrl?.queryParameter("q"))
        assertEquals("alfabetica", products.requestUrl?.queryParameter("ordem"))
        assertEquals("Bearer access", products.getHeader("Authorization")); assertNull(products.getHeader("Cookie"))
        response("""{"itens":[{"id":3,"nome":"Substância real","casNumero":null,"descricao":null,"classes":[],"produtos":[{"id":7,"nome":"Produto real"}]}]}""")
        val substance = repository.substances("u1", "substância", "alfabetica").single()
        assertNull(substance.descricao); assertEquals(7, substance.produtos.single().id)
        val request = server.takeRequest()
        assertEquals("/api/v1/estante/substancias", request.requestUrl?.encodedPath)
        assertEquals("substância", request.requestUrl?.queryParameter("q"))
    }
    @Test fun `missing FDS remains missing and security belongs to requested product`() = runBlocking {
        response(aid); assertFalse(repository.firstAid("u1", 7).encontrada)
        assertEquals("/api/v1/estante/produtos/7/primeiros-socorros", server.takeRequest().path)
        response("""{"produtoId":7,"resumo":null,"versao":null,"dataAtualizacao":null}""")
        assertNull(repository.summary("u1", 7).resumo)
        assertEquals("/api/v1/estante/produtos/7/resumo-simplificado", server.takeRequest().path)
        response(aid.replace("\"produtoId\":7", "\"produtoId\":99"))
        assertTrue(runCatching { repository.firstAid("u1", 7) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `no room is recognized only by its explicit backend error`() = runBlocking {
        response("""{"detail":"produto_sem_comodo"}""", 404)
        assertNull(repository.room("u1", 7))
        assertEquals("/api/v1/estante/produtos/7/comodo", server.takeRequest().path)
        response("""{"detail":"produto_nao_esta_na_estante"}""", 404)
        assertNotNull(runCatching { repository.room("u1", 7) }.exceptionOrNull())
        response("{}", 503)
        assertNotNull(runCatching { repository.room("u1", 7) }.exceptionOrNull())
    }
    @Test fun `removal uses DELETE and requires server confirmation`() = runBlocking {
        response(code = 204); repository.remove("u1", 7)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method); assertEquals("/api/v1/estante/produtos/7", request.path)
        response("{}", 500)
        assertTrue(runCatching { repository.remove("u1", 7) }.isFailure)
        response("{}", 200)
        assertTrue(runCatching { repository.remove("u1", 7) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `history is filtered by product and rejects unrelated records`() = runBlocking {
        val history = """{"itens":[{"id":5,"produtoA":{"id":7,"nome":"Produto real"},"produtoB":{"id":8,"nome":"Outro"},"resultado":"nao_avaliado"}],"pagina":0,"tamanho":20,"totalItens":1,"totalPaginas":1}"""
        response(history); assertEquals("nao_avaliado", repository.history("u1", 7, 0).itens.single().resultado)
        assertEquals("/api/v1/misturas?produtoId=7&page=0&size=20", server.takeRequest().path)
        response(history.replace("\"id\":7", "\"id\":9"))
        assertTrue(runCatching { repository.history("u1", 7, 0) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `session expires after rejected refresh and cannot read another account`() = runBlocking {
        assertTrue(runCatching { repository.products("other", "", null, 0) }.isFailure)
        assertEquals(1, server.requestCount)
        response("{}", 401); response("{}", 401)
        assertTrue(runCatching { repository.products("u1", "", null, 0) }.exceptionOrNull() is CatalogSessionExpired)
        assertEquals(3, server.requestCount)
    }
}
