package com.quimia.android.data.home

import com.quimia.android.data.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HomeRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var sessions: AuthRepository
    private lateinit var home: HomeRepository
    private val now = 1_000_000L
    private val profile = """{"id":"u1","nome":"Nome API","email":"user@example.com","fotoUrl":"https://example.com/photo.png"}"""
    private val history = """{"itens":[{"id":1,"produtoA":{"id":1,"nome":"Produto A","fotoUrl":null},"produtoB":{"id":2,"nome":"Produto B","fotoUrl":"https://example.com/b.png"},"resultado":"nao_avaliado","severidade":null,"descricaoRisco":"Produto sem composição resolvida","consultadoEm":"2026-10-27T12:00:00.123456Z","dosagem":null}],"pagina":0,"tamanho":10,"totalItens":1,"totalPaginas":1}"""
    private fun response(body: String, code: Int = 200) = server.enqueue(MockResponse().setResponseCode(code)
        .setHeader("Content-Type", "application/json").setBody(body))
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val store = object : SessionStore {
            var value: StoredSession? = StoredSession("old-access", "old-refresh", now + 900_000, "u1")
            override fun read() = value
            override fun write(session: StoredSession) { value = session }
            override fun clear() { value = null }
        }
        sessions = AuthRepository(AuthApiFactory.create(server.url("/").toString()), store) { now }
        home = HomeRepository(AuthApiFactory.create(server.url("/").toString(), HomeApi::class.java), sessions)
        response(profile)
        sessions.restore()
        server.takeRequest()
        Unit
    }
    @After fun close() { server.shutdown() }
    @Test fun homeUsesTheAuthenticatedProfileAndPaginatedHistoryWithNoLocalProductFallback() = runBlocking {
        response(profile); response(history)
        assertEquals("Nome API", home.profile("u1").nome)
        val result = home.history("u1", 0, 10)
        assertEquals("Produto A", result.itens.first().produtoA?.nome)
        assertNull(result.itens.first().produtoA?.fotoUrl)
        assertEquals("nao_avaliado", result.itens.first().resultado)
        val me = server.takeRequest()
        val page = server.takeRequest()
        assertEquals("/api/v1/usuarios/me", me.path)
        assertEquals("/api/v1/misturas?page=0&size=10", page.path)
        assertEquals("Bearer old-access", page.getHeader("Authorization"))
        assertNull(page.getHeader("Cookie"))
    }
    @Test fun history401RefreshesOnceAndRetriesWithTheNewAccessToken() = runBlocking {
        response("{}", 401)
        response("""{"accessToken":"new-access","refreshToken":"new-refresh","tokenType":"Bearer","expiresIn":900,"user":$profile}""")
        response(history)
        home.history("u1", 0, 10)
        assertEquals("Bearer old-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("/api/v1/auth/mobile/refresh", server.takeRequest().path)
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
    }
    @Test fun rejectedRefreshExpiresTheSessionWithoutARequestLoop() = runBlocking {
        response("{}", 401); response("{}", 401)
        val error = runCatching { home.history("u1", 0, 10) }.exceptionOrNull()
        assertTrue(error is HomeSessionExpired)
        assertEquals(SessionState.SignedOut, sessions.session.value)
        assertEquals(3, server.requestCount)
    }
    @Test fun aRequestForADifferentAccountIsNeverSentWithTheCurrentToken() = runBlocking {
        assertTrue(runCatching { home.history("other-user", 0, 10) }.isFailure)
        assertEquals(1, server.requestCount)
        assertTrue(sessions.session.value is SessionState.Authenticated)
    }
    @Test fun aProfileFromAnotherAccountIsRejected() = runBlocking {
        response(profile.replace("u1", "other-user"))
        assertTrue(runCatching { home.profile("u1") }.exceptionOrNull() is HomeContractException)
    }
}
