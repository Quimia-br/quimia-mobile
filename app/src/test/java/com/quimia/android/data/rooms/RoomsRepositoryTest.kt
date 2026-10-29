package com.quimia.android.data.rooms

import com.quimia.android.data.auth.*
import com.quimia.android.data.catalog.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RoomsRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RoomsRepository
    private val profile = """{"id":"u1","nome":"Ana","email":"ana@example.com"}"""
    private val products = """{"id":7,"nome":"Produto A","tipo":"limpeza_geral"},{"id":8,"nome":"Produto B","tipo":"outro"}"""
    private val room = """{"id":4,"nome":"Banheiro","icone":"toilet","produtos":[$products],"avaliacao":{"status":"nao_avaliado","totalProdutos":2,"paresAvaliados":0,"paresIncompativeis":0,"severidadeMaxima":null,"incompatibilidades":[]}}"""
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
        val base = server.url("/").toString()
        val sessions = AuthRepository(AuthApiFactory.create(base), Store()) { 1_000_000L }
        response(profile); sessions.restore(); server.takeRequest()
        repository = RoomsRepository(AuthApiFactory.create(base, RoomsApi::class.java), sessions)
        Unit
    }
    @After fun teardown() { server.shutdown() }
    @Test fun `list and detail are scoped to account and use real pagination and query`() = runBlocking {
        response("""{"itens":[{"id":4,"nome":"Banheiro","icone":"toilet","totalProdutos":2,"status":"nao_avaliado"}],"pagina":0,"tamanho":20,"totalItens":1,"totalPaginas":1}""")
        assertEquals(4, repository.list("u1", " Banho ", 0).itens.single().id)
        val request = server.takeRequest()
        assertEquals("/api/v1/comodos", request.requestUrl?.encodedPath)
        assertEquals("Banho", request.requestUrl?.queryParameter("q")); assertEquals("20", request.requestUrl?.queryParameter("size"))
        assertEquals("Bearer access", request.getHeader("Authorization")); assertNull(request.getHeader("Cookie"))
        response(room); assertEquals(setOf(7, 8), repository.detail("u1", 4).produtos.map { it.id }.toSet())
        assertEquals("/api/v1/comodos/4", server.takeRequest().path)
    }
    @Test fun `creation requires 201 and confirmed name icon and product links`() = runBlocking {
        response(room, 201); assertEquals(4, repository.create("u1", " Banheiro ", "toilet", listOf(7, 8, 7)).id)
        val request = server.takeRequest()
        assertEquals("POST", request.method); assertEquals("/api/v1/comodos", request.path)
        assertEquals("""{"nome":" Banheiro ","icone":"toilet","produtoIds":[7,8]}""", request.body.readUtf8())
        response(room, 200)
        assertTrue(runCatching { repository.create("u1", "Banheiro", "toilet", listOf(7, 8)) }.exceptionOrNull() is CatalogContractException)
        response(room.replace("\"icone\":\"toilet\"", "\"icone\":\"pool\""), 201)
        assertTrue(runCatching { repository.create("u1", "Banheiro", "toilet", listOf(7, 8)) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `add and remove affect room endpoint and removal requires 204`() = runBlocking {
        response(room); repository.add("u1", 4, listOf(8))
        val add = server.takeRequest()
        assertEquals("/api/v1/comodos/4/produtos", add.path); assertEquals("""{"produtoIds":[8]}""", add.body.readUtf8())
        response(code = 204); repository.remove("u1", 4, 8)
        val remove = server.takeRequest()
        assertEquals("DELETE", remove.method); assertEquals("/api/v1/comodos/4/produtos/8", remove.path)
        response("{}", 200)
        assertTrue(runCatching { repository.remove("u1", 4, 8) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `shelf selection uses authenticated products instead of samples`() = runBlocking {
        response("""{"itens":[$products],"pagina":0,"tamanho":50,"totalItens":2,"totalPaginas":1}""")
        assertEquals(setOf(7, 8), repository.shelf("u1").map { it.id }.toSet())
        assertEquals("/api/v1/estante/produtos?page=0&size=50", server.takeRequest().path)
    }
    @Test fun `server conflicts remain conflicts and incomplete assessment cannot become safe`() = runBlocking {
        response("""{"detail":"produto_em_outro_comodo"}""", 409)
        val conflict = runCatching { repository.create("u1", "Banheiro", "toilet", listOf(7, 8)) }.exceptionOrNull() as RoomRejected
        assertEquals("produto_em_outro_comodo", conflict.code)
        response(room.replace("nao_avaliado", "seguro"))
        assertTrue(runCatching { repository.detail("u1", 4) }.exceptionOrNull() is CatalogContractException)
        response(room.replace("\"id\":4", "\"id\":99"))
        assertTrue(runCatching { repository.detail("u1", 4) }.exceptionOrNull() is CatalogContractException)
    }
    @Test fun `wrong account never sends request and expired refresh ends session`() = runBlocking {
        assertTrue(runCatching { repository.list("other", "", 0) }.isFailure)
        assertEquals(1, server.requestCount)
        response("{}", 401); response("{}", 401)
        assertTrue(runCatching { repository.list("u1", "", 0) }.exceptionOrNull() is CatalogSessionExpired)
        assertEquals(3, server.requestCount)
    }
}
