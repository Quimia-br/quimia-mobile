package com.quimia.android.data.auth

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var store: MemoryStore
    private lateinit var repository: AuthRepository
    private val now = 1_000_000L
    private val profile = """{"id":"u1","nome":"Ana","email":"ana@example.com","nivelAcesso":"USUARIO"}"""

    @Before fun setup() {
        server = MockWebServer().apply { start() }
        store = MemoryStore()
        repository = AuthRepository(AuthApiFactory.create(server.url("/").toString()), store) { now }
    }
    @After fun teardown() { server.shutdown() }

    private fun response(body: String, status: Int = 200) = server.enqueue(
        MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body)
    )
    private fun session(access: String = "a1", refresh: String = "r1") =
        """{"accessToken":"$access","refreshToken":"$refresh","tokenType":"Bearer","expiresIn":900,"user":$profile}"""
    private fun seed(expired: Boolean = false) {
        store.value = StoredSession("old-access", "old-refresh", if (expired) now - 1 else now + 900_000, "u1")
    }

    @Test fun `login uses mobile JSON and only unlocks after authenticated profile`() = runBlocking {
        response(session())
        response(profile)
        repository.login(" ana@example.com ", "password")
        val login = server.takeRequest()
        assertEquals("/api/v1/auth/mobile/login", login.path)
        assertEquals("POST", login.method)
        assertEquals("""{"email":"ana@example.com","senha":"password"}""", login.body.readUtf8())
        assertNull(login.getHeader("Cookie"))
        val me = server.takeRequest()
        assertEquals("/api/v1/usuarios/me", me.path)
        assertEquals("Bearer a1", me.getHeader("Authorization"))
        assertTrue(repository.session.value is SessionState.Authenticated)
        assertEquals("r1", store.value?.refreshToken)
    }

    @Test fun `login failure never unlocks app`() = runBlocking {
        response("""{"detail":"invalid_credentials"}""", 401)
        val error = runCatching { repository.login("a", "wrong") }.exceptionOrNull() as Exception
        assertEquals("E-mail ou senha incorretos.", authErrorMessage(error))
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `Google sends a Firebase token to the API and still verifies the backend user`() = runBlocking {
        response(session())
        response(profile)
        repository.loginWithFirebaseToken("firebase-id-token")
        val login = server.takeRequest()
        assertEquals("/api/v1/auth/firebase", login.path)
        assertEquals("""{"idToken":"firebase-id-token"}""", login.body.readUtf8())
        assertEquals("Bearer a1", server.takeRequest().getHeader("Authorization"))
        assertEquals("u1", (repository.session.value as SessionState.Authenticated).profile.id)
    }

    @Test fun `Google completion persists profile and address through authenticated API routes`() = runBlocking {
        response(session()); response(profile)
        repository.loginWithFirebaseToken("firebase-id-token")
        response(profile.dropLast(1) + ",\"dataNasc\":\"2000-04-20\"}")
        response("""{"cep":"01001-000","estado":"SP","bairro":"Sé","rua":"Praça da Sé","numero":100,"complemento":null}""")
        repository.completeInitialProfile("Ana", "2000-04-20")
        repository.saveInitialAddress(AuthAddress("01001000", "SP", "Sé", "Praça da Sé", 100))
        server.takeRequest(); server.takeRequest()
        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/api/v1/usuarios/me", update.path)
        assertEquals("Bearer a1", update.getHeader("Authorization"))
        assertEquals("""{"nome":"Ana","dataNasc":"2000-04-20"}""", update.body.readUtf8())
        val address = server.takeRequest()
        assertEquals("PUT", address.method)
        assertEquals("/api/v1/usuarios/me/localizacao", address.path)
        assertEquals("Bearer a1", address.getHeader("Authorization"))
    }

    @Test fun `profile edit rejects unconfirmed name and keeps authenticated identity`() = runBlocking {
        response(session()); response(profile)
        repository.login("ana@example.com", "password")
        response(profile.dropLast(1) + ",\"dataNasc\":\"2000-04-20\"}")
        assertTrue(runCatching { repository.completeInitialProfile("Outro nome", "2000-04-20") }.isFailure)
        assertEquals("Ana", (repository.session.value as SessionState.Authenticated).profile.nome)
    }

    @Test fun `profile refresh cannot read a different account`() = runBlocking {
        response(session()); response(profile)
        repository.login("ana@example.com", "password")
        assertTrue(runCatching { repository.reloadProfile("other-user") }.isFailure)
        assertEquals(2, server.requestCount)
    }

    @Test fun `unconfirmed profile changes cannot complete onboarding`() = runBlocking {
        response(session()); response(profile)
        repository.loginWithFirebaseToken("firebase-id-token")
        response(profile)
        assertTrue(runCatching { repository.completeInitialProfile("Ana", "2000-04-20") }.isFailure)
    }

    @Test fun `different address returned by the backend is an error instead of a fabricated save`() = runBlocking {
        response(session()); response(profile)
        repository.loginWithFirebaseToken("firebase-id-token")
        response("""{"cep":"01001-000","estado":"SP","bairro":"Sé","rua":"Praça da Sé","numero":200}""")
        assertTrue(runCatching { repository.saveInitialAddress(AuthAddress("01001000", "SP", "Sé", "Praça da Sé", 100)) }.isFailure)
    }

    @Test fun `rejected Google exchange cannot authenticate with Firebase alone`() = runBlocking {
        response("""{"detail":"firebase_disabled"}""", 503)
        val error = runCatching { repository.loginWithFirebaseToken("firebase-id-token") }.exceptionOrNull() as Exception
        assertTrue(authErrorMessage(error).contains("desativado no servidor"))
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `malformed session is not saved or authenticated`() = runBlocking {
        response(session().replace("\"Bearer\"", "\"Basic\""))
        assertTrue(runCatching { repository.login("a", "password") }.isFailure)
        assertNull(store.value)
        assertEquals(1, server.requestCount)
    }

    @Test fun `profile from another account cannot unlock the app`() = runBlocking {
        response(session())
        response(profile.replace("u1", "other-user"))
        assertTrue(runCatching { repository.login("a", "password") }.isFailure)
        assertNull(store.value)
        assertEquals(SessionState.SignedOut, repository.session.value)
    }

    @Test fun `refresh cannot switch the account identity`() = runBlocking {
        seed(expired = true)
        response(session().replace("u1", "other-user"))
        repository.restore()
        assertEquals(1, server.requestCount)
        assertNull(store.value)
        assertEquals(SessionState.SignedOut, repository.session.value)
    }

    @Test fun `profile failure after login does not grant access`() = runBlocking {
        response(session())
        response("{}", 503)
        assertTrue(runCatching { repository.login("a", "password") }.isFailure)
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `expired session rotates both tokens before profile`() = runBlocking {
        seed(expired = true)
        response(session("new-access", "new-refresh"))
        response(profile)
        repository.restore()
        val refresh = server.takeRequest()
        assertEquals("/api/v1/auth/mobile/refresh", refresh.path)
        assertEquals("""{"refreshToken":"old-refresh"}""", refresh.body.readUtf8())
        assertNull(refresh.getHeader("Cookie"))
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("new-refresh", store.value?.refreshToken)
    }

    @Test fun `401 retries once with new bearer and logout uses latest refresh`() = runBlocking {
        seed()
        response("{}", 401)
        response(session("a2", "r2"))
        response(profile)
        repository.restore()
        response("", 204)
        assertTrue(repository.logout())
        assertEquals("Bearer old-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("/api/v1/auth/mobile/refresh", server.takeRequest().path)
        assertEquals("Bearer a2", server.takeRequest().getHeader("Authorization"))
        val logout = server.takeRequest()
        assertEquals("/api/v1/auth/mobile/logout", logout.path)
        assertEquals("Bearer a2", logout.getHeader("Authorization"))
        assertEquals("", logout.body.readUtf8())
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `rejected refresh clears credentials and is never replayed`() = runBlocking {
        seed(expired = true)
        response("{}", 401)
        repository.restore()
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
        repository.restore()
        assertEquals(1, server.requestCount)
    }

    @Test fun `ambiguous refresh server failure also discards old token`() = runBlocking {
        seed(expired = true)
        response("{}", 503)
        repository.restore()
        repository.restore()
        assertNull(store.value)
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertEquals(1, server.requestCount)
    }

    @Test fun `second 401 stops retry loop and clears session`() = runBlocking {
        seed()
        response("{}", 401)
        response(session())
        response("{}", 401)
        repository.restore()
        assertEquals(3, server.requestCount)
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `refresh is never replayed even when server requests automatic retry`() = runBlocking {
        seed(expired = true)
        server.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", "0"))
        repository.restore()
        assertEquals(1, server.requestCount)
        assertNull(store.value)
        assertEquals(SessionState.SignedOut, repository.session.value)
    }

    @Test fun `forbidden does not trigger token refresh or authorize startup`() = runBlocking {
        seed()
        response("{}", 403)
        repository.restore()
        assertTrue(repository.session.value is SessionState.Unavailable)
        assertEquals(1, server.requestCount)
        assertEquals("old-refresh", store.value?.refreshToken)
    }

    @Test fun `server unavailable during restore keeps tokens but blocks screens`() = runBlocking {
        seed()
        response("{}", 503)
        repository.restore()
        assertTrue(repository.session.value is SessionState.Unavailable)
        assertNotNull(store.value)
        response(profile)
        repository.restore()
        assertTrue(repository.session.value is SessionState.Authenticated)
    }

    @Test fun `logout failure still clears local session`() = runBlocking {
        seed()
        response(profile)
        repository.restore()
        response("{}", 503)
        assertFalse(repository.logout())
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertNull(store.value)
    }

    @Test fun `concurrent protected requests serialize rotation`() = runBlocking {
        seed()
        response(profile)
        repository.restore()
        response("{}", 401)
        response(session("a2", "r2"))
        response(profile)
        response(profile)
        coroutineScope {
            val first = async { repository.reloadProfile() }
            val second = async { repository.reloadProfile() }
            first.await()
            second.await()
        }
        val requests = (1..5).map { server.takeRequest(1, TimeUnit.SECONDS)!! }
        assertEquals(1, requests.count { it.path == "/api/v1/auth/mobile/refresh" })
        assertEquals("Bearer a2", requests.last().getHeader("Authorization"))
        assertEquals("r2", store.value?.refreshToken)
    }

    @Test fun `no stored session makes no network request`() = runBlocking {
        repository.restore()
        assertEquals(SessionState.SignedOut, repository.session.value)
        assertEquals(0, server.requestCount)
    }

    @Test fun `session values are redacted in string representation`() {
        val value = StoredSession("secret-access", "secret-refresh", now, "u1").toString()
        assertFalse(value.contains("secret-access"))
        assertFalse(value.contains("secret-refresh"))
    }

    private class MemoryStore : SessionStore {
        var value: StoredSession? = null
        override fun read() = value
        override fun write(session: StoredSession) { value = session }
        override fun clear() { value = null }
    }
}
