package com.quimia.android.data.mixtures

import com.quimia.android.data.auth.*
import com.quimia.android.presentation.screens.home.HomeAssessment
import com.quimia.android.presentation.screens.home.assessment
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MixturesRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: MixturesRepository
    private class Store : SessionStore {
        private var value: StoredSession? = StoredSession("access", "refresh", 2_000_000L, "u1")
        override fun read() = value
        override fun write(session: StoredSession) { value = session }
        override fun clear() { value = null }
    }
    private fun response(body: String, status: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    }
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val base = server.url("/").toString()
        val sessions = AuthRepository(AuthApiFactory.create(base), Store()) { 1_000_000L }
        response("""{"id":"u1","nome":"Ana","email":"ana@example.com"}""")
        sessions.restore(); server.takeRequest()
        repository = MixturesRepository(AuthApiFactory.create(base, MixturesApi::class.java), sessions)
    }
    @After fun teardown() { server.shutdown() }
    private fun analysis(result: String = "incompativel", severity: String = "\"critica\"") =
        """{"id":7,"produtoA":{"id":2,"nome":"Produto A"},"produtoB":{"id":11,"nome":"Produto B"},"resultado":"$result","severidade":$severity,"descricaoRisco":"Resposta retornada pela API","consultadoEm":"2026-10-30T10:00:00Z","dosagem":null}"""

    @Test fun `picker loads every shelf page including products without a type`() = runBlocking {
        fun product(id: Int) = """{"id":$id,"nome":"Produto $id","tipo":null}"""
        response("""{"itens":[${(1..50).joinToString(",") { product(it) }}],"pagina":0,"tamanho":50,"totalItens":51,"totalPaginas":2}""")
        response("""{"itens":[${product(51)}],"pagina":1,"tamanho":50,"totalItens":51,"totalPaginas":2}""")
        val products = repository.shelf("u1")
        assertEquals((1..51).toList(), products.map { it.id })
        assertNull(products.last().tipo)
        assertEquals("/api/v1/estante/produtos?page=0&size=50", server.takeRequest().path)
        assertEquals("/api/v1/estante/produtos?page=1&size=50", server.takeRequest().path)
    }
    @Test fun `empty shelf stays empty and a failed request is not an empty shelf`() = runBlocking {
        response("""{"itens":[],"pagina":0,"tamanho":50,"totalItens":0,"totalPaginas":0}""")
        assertTrue(repository.shelf("u1").isEmpty())
        response("{}", 503)
        assertTrue(runCatching { repository.shelf("u1") }.exceptionOrNull() is retrofit2.HttpException)
    }
    @Test fun `real ids are posted and canonical response order is accepted`() = runBlocking {
        response(analysis(), 201)
        val result = repository.analyze("u1", 11, 2)
        assertEquals(7, result.id)
        assertEquals("incompativel", result.resultado)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/misturas", request.path)
        assertEquals("Bearer access", request.getHeader("Authorization"))
        assertEquals("""{"produtoIdA":11,"produtoIdB":2}""", request.body.readUtf8())
    }
    @Test fun `missing chemical data remains unassessed with the API reason`() = runBlocking {
        response(analysis("nao_avaliado", "null"), 201)
        val result = repository.analyze("u1", 2, 11)
        assertEquals(HomeAssessment.Unassessed, result.assessment())
        assertEquals("Resposta retornada pela API", result.descricaoRisco)
        assertNull(result.dosagem)
    }
    @Test fun `another pair or an invalid assessment never becomes a successful analysis`() = runBlocking {
        response(analysis(), 201)
        assertTrue(runCatching { repository.analyze("u1", 2, 113) }.exceptionOrNull() is MixturesContractException)
        response(analysis("compativel"), 201)
        assertTrue(runCatching { repository.analyze("u1", 2, 11) }.exceptionOrNull() is MixturesContractException)
    }
    @Test fun `history uses the authenticated API and product filter`() = runBlocking {
        response("""{"itens":[${analysis()}],"pagina":0,"tamanho":20,"totalItens":1,"totalPaginas":1}""")
        assertEquals(7, repository.history("u1", productId = 2).itens.single().id)
        val request = server.takeRequest()
        assertEquals("/api/v1/misturas?page=0&size=20&produtoId=2", request.path)
        assertEquals("Bearer access", request.getHeader("Authorization"))
    }
}
