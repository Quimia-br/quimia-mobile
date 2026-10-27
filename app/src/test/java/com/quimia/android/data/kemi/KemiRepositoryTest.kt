package com.quimia.android.data.kemi

import com.quimia.android.data.auth.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KemiRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: KemiRepository
    @Before fun setup() = runBlocking {
        server = MockWebServer().apply { start() }
        val store = object : SessionStore {
            override fun read(): StoredSession? = StoredSession("access", "refresh", System.currentTimeMillis() + 900000, "u1")
            override fun write(session: StoredSession) { }
            override fun clear() { }
        }
        val auth = AuthRepository(AuthApiFactory.create(server.url("/").toString()), store)
        respond("""{"id":"u1","nome":"Ana","email":"ana@example.com"}"""); auth.restore(); server.takeRequest()
        repository = KemiRepository(AuthApiFactory.create(server.url("/").toString(), KemiApi::class.java), auth)
    }
    @After fun close() { server.shutdown() }
    private fun respond(body: String, code: Int = 200) { server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)) }
    private fun answer(session: String = "chat-1", text: String = "Resposta real", timestamp: String = "2026-10-27T12:34:56.123456789-03:00") =
        """{"texto":"$text","sessaoId":"$session","respondidoEm":"$timestamp"}"""
    @Test fun `every message keeps its API session and sends only user text with bearer`() = runBlocking {
        repeat(2) {
            respond(answer())
            assertEquals("Resposta real", repository.send("u1", "chat-1", "  Pergunta  ").texto)
            val request = server.takeRequest()
            assertEquals("/api/v1/chat/mensagens", request.path)
            assertEquals("POST", request.method)
            assertEquals("Bearer access", request.getHeader("Authorization"))
            assertNull(request.getHeader("Cookie"))
            assertEquals("""{"texto":"Pergunta","sessaoId":"chat-1"}""", request.body.readUtf8())
        }
    }
    @Test fun `another conversation blank text and invalid timestamp cannot become successful replies`() = runBlocking {
        listOf(answer(session = "another-chat"), answer(text = ""), answer(timestamp = "invalid")).forEach {
            respond(it)
            assertTrue(runCatching { repository.send("u1", "chat-1", "Pergunta") }.isFailure)
        }
    }
    @Test fun `HTML with status 200 is rejected instead of displayed as a Kemi reply`() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "text/html").setBody("<html><title>Erro Interno</title></html>"))
        assertTrue(runCatching { repository.send("u1", "chat-1", "Pergunta") }.isFailure)
        assertEquals(2, server.requestCount)
    }
    @Test fun `rate limit never retries automatically`() = runBlocking {
        respond("""{"detail":"limite_excedido"}""", 429)
        assertTrue(runCatching { repository.send("u1", "chat-1", "Pergunta") }.isFailure)
        assertEquals(2, server.requestCount)
    }
    @Test fun `invalid session text length and other account never send a request`() = runBlocking {
        assertTrue(runCatching { repository.send("u1", "bad id", "Pergunta") }.isFailure)
        assertTrue(runCatching { repository.send("u1", "chat-1", "a".repeat(2001)) }.isFailure)
        assertTrue(runCatching { repository.send("u2", "chat-1", "Pergunta") }.isFailure)
        assertEquals(1, server.requestCount)
    }
    @Test fun `API timestamps preserve instant including fractional seconds and offsets`() {
        assertEquals(kemiResponseTime("2026-10-27T15:34:56.123Z"), kemiResponseTime("2026-10-27T12:34:56.123456789-03:00"))
        assertNotNull(kemiResponseTime("2026-10-27T15:34:56Z"))
        assertNull(kemiResponseTime("2026-02-30T15:34:56Z"))
    }
}
