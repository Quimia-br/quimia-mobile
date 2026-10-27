package com.quimia.android.data.proximity

import com.quimia.android.data.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProximityRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ProximityRepository
    private lateinit var auth: AuthRepository
    private val user = """{"id":"u1","nome":"Ana","email":"ana@example.com"}"""
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val store = object : SessionStore {
            override fun read(): StoredSession? = StoredSession("access", "refresh", System.currentTimeMillis() + 900000, "u1")
            override fun write(value: StoredSession) { }
            override fun clear() { }
        }
        auth = AuthRepository(AuthApiFactory.create(server.url("/").toString()), store)
        respond(user); auth.restore(); server.takeRequest()
        repository = ProximityRepository(AuthApiFactory.create(server.url("/").toString(), ProximityApi::class.java), auth)
    }
    @After fun close() { server.shutdown() }
    private fun respond(body: String, code: Int = 200) { server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)) }
    @Test fun `points send category address filter and bearer without made up coordinates`() = runBlocking {
        respond("""{"itens":[{"id":2,"nome":"Coleta","tipo":"descarte","rua":"Rua A","estado":"SP"}],"pagina":0,"tamanho":20,"totalItens":1,"totalPaginas":1}""")
        val result = repository.points("u1", "descarte", true, 0)
        val request = server.takeRequest()
        assertEquals("Bearer access", request.getHeader("Authorization"))
        assertEquals("descarte", request.requestUrl!!.queryParameter("tipo"))
        assertEquals("true", request.requestUrl!!.queryParameter("usarLocalizacaoCadastrada"))
        assertEquals("Rua A, SP", result.itens.single().mapsQuery)
    }
    @Test fun `missing address is different from failed address request`() = runBlocking {
        respond("""{"detail":"not_found"}""", 404)
        assertNull(repository.address("u1"))
        respond("""{"detail":"other_error"}""", 404)
        assertTrue(runCatching { repository.address("u1") }.isFailure)
    }
    @Test fun `invalid pagination or wrong category is not an empty success`() = runBlocking {
        respond("""{"itens":[],"pagina":0,"tamanho":20,"totalItens":80,"totalPaginas":1}""")
        assertTrue(runCatching { repository.points("u1", "compra", false, 0) }.isFailure)
    }
    @Test fun `switching account never sends old account request`() = runBlocking {
        assertTrue(runCatching { repository.points("u2", "compra", false, 0) }.isFailure)
        assertEquals(1, server.requestCount)
    }
    @Test fun `incomplete API address never invents routing information`() {
        assertNull(ApiPoint(1, "Ponto", "compra", bairro = "Centro").mapsQuery)
        assertEquals("Endereço não informado pela API", ApiPoint(1, "Ponto", "compra").address)
    }
}
