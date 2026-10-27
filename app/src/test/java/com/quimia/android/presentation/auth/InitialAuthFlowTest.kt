package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.*
import com.quimia.android.presentation.components.BirthDate
import com.quimia.android.presentation.screens.auth.*
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.serialization.json.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InitialAuthFlowTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AuthRepository
    private lateinit var flow: InitialAuthFlow
    private var now = 1_000_000L
    private val profile = """{"id":"u1","nome":"Ana Teste","email":"ana@example.com","dataNasc":"2000-04-20"}"""
    private val session get() = """{"accessToken":"access","refreshToken":"refresh","tokenType":"Bearer","expiresIn":900,"user":$profile}"""
    private val details get() = RegistrationDetails(" Ana Teste ", "20/04/2000", " ana@example.com ", "SenhaForte42!")
    private val address = AuthAddress("01001000", "SP", "Sé", "Praça da Sé", 100, "Apto 1")

    @Before fun setup() = runBlocking {
        now = 1_000_000L
        server = MockWebServer().apply { start() }
        repository = AuthRepository(AuthApiFactory.create(server.url("/").toString()), object : SessionStore {
            private var stored: StoredSession? = null
            override fun read() = stored
            override fun write(session: StoredSession) { stored = session }
            override fun clear() { stored = null }
        }) { now }
        repository.restore()
        flow = InitialAuthFlow(repository) { now }
    }
    @After fun close() { server.shutdown() }
    private fun response(body: String, status: Int = 200) = server.enqueue(
        MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    private fun challenge() = response("""{"challengeToken":"challenge","expiresIn":900}""")
    private fun grant() = response("""{"resetToken":"reset","expiresIn":600}""")

    @Test fun registrationSendsTheEnteredDetailsAndAddressBeforeLogin() = runBlocking {
        flow.prepareRegistration(details)
        response(profile, 201); response(session); response(profile)
        assertEquals("u1", flow.finishRegistration(address).id)
        val register = server.takeRequest()
        assertEquals("/api/v1/auth/register", register.path)
        val body = Json.parseToJsonElement(register.body.readUtf8()).jsonObject
        assertEquals("Ana Teste", body["nome"]?.jsonPrimitive?.content)
        assertEquals("ana@example.com", body["email"]?.jsonPrimitive?.content)
        assertEquals("2000-04-20", body["dataNasc"]?.jsonPrimitive?.content)
        assertEquals(100, body["localizacao"]?.jsonObject?.get("numero")?.jsonPrimitive?.int)
        assertNull(body["localizacao"]?.jsonObject?.get("cidade"))
        assertNull(register.getHeader("Authorization"))
        assertEquals("/api/v1/auth/mobile/login", server.takeRequest().path)
        assertEquals("/api/v1/usuarios/me", server.takeRequest().path)
        assertFalse(flow.hasRegistration)
    }
    @Test fun skippingAddressReallyRegistersWithoutAnInventedAddress() = runBlocking {
        flow.prepareRegistration(details)
        response(profile, 201); response(session); response(profile)
        flow.finishRegistration(null)
        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertFalse(body.containsKey("localizacao"))
    }
    @Test fun registrationFailureDoesNotTryLoginOrAuthenticate() = runBlocking {
        flow.prepareRegistration(details)
        response("""{"detail":"email_in_use"}""", 409)
        val error = runCatching { flow.finishRegistration(null) }.exceptionOrNull() as Exception
        assertTrue(authErrorMessage(error).contains("já tem cadastro"))
        assertEquals(1, server.requestCount)
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun retryingAfterAccountCreationDoesNotCreateTheSameAccountAgain() = runBlocking {
        flow.prepareRegistration(details)
        response(profile, 201); response("{}", 503)
        assertTrue(runCatching { flow.finishRegistration(address) }.isFailure)
        response(session); response(profile)
        flow.finishRegistration(address)
        val paths = (1..4).map { server.takeRequest().path }
        assertEquals(1, paths.count { it == "/api/v1/auth/register" })
        assertEquals(SessionState.Authenticated::class, repository.session.value::class)
    }
    @Test fun changingAddressAfterAccountCreationIsNotPretendedToBeSaved() = runBlocking {
        flow.prepareRegistration(details)
        response(profile, 201); response("{}", 503)
        assertTrue(runCatching { flow.finishRegistration(address) }.isFailure)
        assertTrue(runCatching { flow.finishRegistration(address.copy(numero = 200)) }.isFailure)
        assertEquals(2, server.requestCount)
    }
    @Test fun recoveryRequiresTheServerChallengeThenGrantThenSuccessfulReset() = runBlocking {
        challenge(); grant(); response("", 204)
        flow.requestCode(" ana@example.com ")
        assertTrue(flow.hasChallenge)
        flow.verifyCode("1234")
        assertFalse(flow.hasChallenge)
        assertTrue(flow.hasGrant)
        flow.resetPassword("NovaSenha42!")
        assertFalse(flow.hasGrant)
        val forgot = server.takeRequest()
        assertEquals("/api/v1/auth/forgot-password", forgot.path)
        assertEquals("""{"email":"ana@example.com"}""", forgot.body.readUtf8())
        val verify = server.takeRequest()
        assertEquals("/api/v1/auth/verify-recovery-code", verify.path)
        assertEquals("""{"email":"ana@example.com","code":"1234","challengeToken":"challenge"}""", verify.body.readUtf8())
        val reset = server.takeRequest()
        assertEquals("/api/v1/auth/reset-password", reset.path)
        assertEquals("""{"resetToken":"reset","novaSenha":"NovaSenha42!"}""", reset.body.readUtf8())
        assertNull(verify.getHeader("Authorization"))
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun rejectedCodeNeverAllowsResetAndCanBeRetried() = runBlocking {
        challenge(); response("""{"detail":"invalid_recovery_code"}""", 400)
        flow.requestCode("ana@example.com")
        assertTrue(runCatching { flow.verifyCode("0000") }.isFailure)
        assertFalse(flow.hasGrant)
        assertTrue(runCatching { flow.resetPassword("NovaSenha42!") }.isFailure)
        assertEquals(2, server.requestCount)
        assertTrue(flow.hasChallenge)
    }
    @Test fun expiredChallengeAndResetGrantCannotBeUsed() = runBlocking {
        challenge()
        flow.requestCode("ana@example.com")
        now += 900_000
        assertFalse(flow.hasChallenge)
        assertTrue(runCatching { flow.verifyCode("1234") }.isFailure)
        assertEquals(1, server.requestCount)
        challenge(); grant()
        flow.requestCode("ana@example.com"); flow.verifyCode("1234")
        now += 600_000
        assertTrue(runCatching { flow.resetPassword("NovaSenha42!") }.isFailure)
        assertEquals(3, server.requestCount)
    }
    @Test fun failedResetKeepsTheGrantForRetryAndDoesNotReportSuccess() = runBlocking {
        challenge(); grant(); response("{}", 503)
        flow.requestCode("ana@example.com"); flow.verifyCode("1234")
        assertTrue(runCatching { flow.resetPassword("NovaSenha42!") }.isFailure)
        assertTrue(flow.hasGrant)
        response("", 204)
        flow.resetPassword("NovaSenha42!")
        assertFalse(flow.hasGrant)
    }
    @Test fun noDataAndMalformedBackendChallengesCannotAdvanceTheFlow() = runBlocking {
        assertTrue(runCatching { flow.finishRegistration(null) }.isFailure)
        assertTrue(runCatching { flow.verifyCode("1234") }.isFailure)
        assertTrue(runCatching { flow.resetPassword("NovaSenha42!") }.isFailure)
        assertEquals(0, server.requestCount)
        response("""{"challengeToken":"","expiresIn":900}""")
        assertTrue(runCatching { flow.requestCode("ana@example.com") }.isFailure)
        assertFalse(flow.hasChallenge)
    }
    @Test fun passwordsAgeAndAddressFollowTheApiRules() {
        assertNull(authNewPasswordError("SenhaForte42!"))
        assertNotNull(authNewPasswordError("12345678"))
        assertNotNull(authNewPasswordError("abcdefgh"))
        assertNotNull(authNewPasswordError("é".repeat(36) + "A1"))
        assertNull(authRegistrationBirthDateError("27/10/2008", BirthDate(2026, 10, 27)))
        assertNotNull(authRegistrationBirthDateError("28/10/2008", BirthDate(2026, 10, 27)))
        val value = NearbyAddress("01001-000", "SP", "São Paulo", "Sé", "Praça da Sé", "100", "")
        assertEquals("01001000", initialApiAddress(value).cep)
        assertEquals(null, initialApiAddress(value).complemento)
        assertTrue(runCatching { initialApiAddress(value.copy(number = "100A")) }.isFailure)
        assertTrue(runCatching { initialApiAddress(value.copy(number = "0")) }.isFailure)
        assertFalse(details.toString().contains(details.password))
    }
}
