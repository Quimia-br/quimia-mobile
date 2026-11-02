package com.quimia.android.data.kemi

import com.quimia.android.data.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.HttpException

class KemiRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: KemiRepository
    private class Store : SessionStore {
        var value: StoredSession? = StoredSession("access", "refresh", 2_000_000L, "u1")
        override fun read() = value
        override fun write(session: StoredSession) { value = session }
        override fun clear() { value = null }
    }
    private fun response(body: String, status: Int = 200, type: String = "application/json") {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", type).setBody(body))
    }
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val base = server.url("/").toString()
        val sessions = AuthRepository(AuthApiFactory.create(base), Store()) { 1_000_000L }
        response("""{"id":"u1","nome":"Ana","email":"ana@example.com"}""")
        sessions.restore(); server.takeRequest()
        repository = KemiRepository(AuthApiFactory.create(base, KemiApi::class.java), sessions)
        Unit
    }
    @After fun teardown() { server.shutdown() }

    @Test fun `messages use mobile JWT and preserve session across turns without posting history or user id`() = runBlocking {
        repeat(2) {
            response("""{"texto":"Resposta real","sessaoId":"chat-123","respondidoEm":"2026-11-02T10:00:00Z"}""")
            assertEquals("Resposta real", repository.send("u1", "chat-123", "  Pergunta $it  "))
            val request = server.takeRequest()
            assertEquals("/api/v1/chat/mensagens", request.path)
            assertEquals("Bearer access", request.getHeader("Authorization"))
            assertEquals("""{"texto":"Pergunta $it","sessaoId":"chat-123"}""", request.body.readUtf8())
        }
    }
    @Test fun `invalid input and changed account never contact chat service`() = runBlocking {
        val before = server.requestCount
        for ((user,session,text) in listOf(Triple("u2","chat","Olá"), Triple("u1","bad/session","Olá"),
            Triple("u1","chat"," "), Triple("u1","chat","a".repeat(2001)))) {
            assertTrue(runCatching { repository.send(user,session,text) }.isFailure)
        }
        assertEquals(before, server.requestCount)
    }
    @Test fun `blank mismatched and html answers cannot become assistant messages`() = runBlocking {
        for (body in listOf("""{"texto":" ","sessaoId":"chat"}""",
            """{"texto":"Resposta","sessaoId":"outra"}""",
            """{"texto":"<!doctype html><html>Erro Interno</html>","sessaoId":"chat"}""")) {
            response(body)
            assertTrue(runCatching { repository.send("u1","chat","Olá") }.exceptionOrNull() is KemiContractException)
            server.takeRequest()
        }
        response("<html>Erro Interno</html>", type = "text/html")
        assertTrue(runCatching { repository.send("u1","chat","Olá") }.isFailure)
        server.takeRequest()
        response("""{"texto":"Resposta","sessaoId":"chat"}""", type = "text/html")
        assertTrue(runCatching { repository.send("u1","chat","Olá") }.exceptionOrNull() is KemiContractException)
        server.takeRequest()
        Unit
    }
    @Test fun `limit and service errors preserve status and never silently retry a message`() = runBlocking {
        for (status in listOf(429,502,503,504)) {
            val before = server.requestCount
            response("""{"detail":"indisponivel"}""", status)
            assertEquals(status, (runCatching { repository.send("u1","chat","Olá") }.exceptionOrNull() as HttpException).code())
            assertEquals(before+1, server.requestCount)
            server.takeRequest()
        }
    }
    @Test fun `new chat requires a matching confirmed remote closure`() = runBlocking {
        for (status in listOf("resumida","ja_resumida","sem_turnos")) {
            response("""{"sessaoId":"chat","situacao":"$status"}""")
            repository.close("u1","chat")
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/chat/sessoes/chat/encerrar", request.path)
            assertEquals("Bearer access", request.getHeader("Authorization"))
        }
        response("""{"sessaoId":"chat","situacao":"desconhecida"}""")
        assertTrue(runCatching { repository.close("u1","chat") }.exceptionOrNull() is KemiContractException)
        server.takeRequest()
        response("""{"sessaoId":"outra","situacao":"resumida"}""")
        assertTrue(runCatching { repository.close("u1","chat") }.exceptionOrNull() is KemiContractException)
        server.takeRequest()
        Unit
    }
}
