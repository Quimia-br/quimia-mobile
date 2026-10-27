package com.quimia.android.data.project

import com.quimia.android.presentation.components.BirthDate
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.kemi.*
import com.quimia.android.presentation.screens.profile.ProfileDetails
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import com.quimia.android.presentation.screens.rooms.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProjectUiAdaptersTest {
    private val user = ApiUser("user-1", "Nome da API", "qa@example.invalid", "2000-04-20")
    private fun contractError(operation: () -> Unit) {
        try { operation(); fail("Um contrato incompleto não pode virar sucesso") }
        catch (_: ApiContractException) { }
    }

    @Test fun convertsTheFullBirthdayWithoutInventingAYear() {
        assertEquals("20/04/2000", birthDateFromApi("2000-04-20"))
        assertEquals("2000-04-20", birthDateToApi("20/04/2000"))
        assertEquals("", birthDateFromApi(null))
        contractError { birthDateFromApi("2000-02-31") }
        contractError { birthDateFromApi("20/04/2000") }
        contractError { birthDateToApi("20/04") }
    }

    @Test fun birthdayMustBeStrictlyInThePastAsRequiredByTheApi() {
        contractError { birthDateToApi(BirthDate.today().toString()) }
    }

    @Test fun profileUpdateUsesOnlyFieldsSupportedByTheBackend() {
        val body = profileUpdateBody(ProfileDetails("Novo nome", "29/02/2000", user.email), user)
        assertEquals(setOf("nome", "dataNasc"), body.keys)
        assertEquals("2000-02-29", body.getValue("dataNasc").jsonPrimitive.content)
    }

    @Test fun emailChangesAndUnsupportedBirthdayDeletionAreReported() {
        contractError { profileUpdateBody(ProfileDetails("Nome", "20/04/2000", "other@example.invalid"), user) }
        contractError { profileUpdateBody(ProfileDetails("Nome", "", user.email), user) }
        assertEquals(setOf("nome"), profileUpdateBody(ProfileDetails("Nome", "", user.email), user.copy(dataNasc = null)).keys)
    }

    @Test fun savesSupportedAddressFieldsWithoutClaimingThatTheApiStoresTheCity() {
        val address = NearbyAddress("01001000", "SP", "São Paulo", "Sé", "Praça da Sé", "100", "Teste")
        val body = address.toApiAddress()
        assertEquals("Praça da Sé", body.rua)
        assertEquals(100, body.numero)
        assertEquals("", body.toNearbyFields()[2])
        assertFalse(Json.encodeToString(body).contains("cidade"))
        contractError { address.copy(number = "0").toApiAddress() }
    }

    @Test fun usesTheResponseTimestampIncludingFractionalSecondsAndOffset() {
        assertEquals(0L, apiResponseTime("1970-01-01T00:00:00Z"))
        assertEquals(123L, apiResponseTime("1970-01-01T00:00:00.123456789Z"))
        assertEquals(123L, apiResponseTime("1969-12-31T21:00:00.123-03:00"))
        contractError { apiResponseTime("2026-02-31T00:00:00Z") }
        contractError { apiResponseTime("2026-01-01T00:00:00") }
    }

    @Test fun chatReplyUsesOnlyTheTextSessionAndTimeReturnedByTheApi() {
        val reply = ApiChatReply("Resposta real", "remote-session", "1970-01-01T00:00:00Z").toKemiReply()
        assertEquals(KemiReply.Answer("Resposta real", "remote-session", 0L), reply)
        contractError { ApiChatReply(" ", "remote-session", "1970-01-01T00:00:00Z").toKemiReply() }
        contractError { ApiChatReply("Resposta", "invalid/session", "1970-01-01T00:00:00Z").toKemiReply() }
    }

    @Test fun chatKeepsItsRemoteSessionAcrossRestorationAndStartsNewChatsWithoutReusingIt() {
        val controller = KemiController()
        val first = requireNotNull(controller.send("Pergunta", 1000L))
        controller.complete(first, KemiReply.Answer("Resposta", "server-session", 2000L))
        val restored = KemiController(restoreKemiState(Json.encodeToString(controller.state)))
        val second = requireNotNull(restored.send("Continuação", 3000L))
        assertEquals("server-session", second.conversation.apiSessionId)
        assertEquals(2000L, second.conversation.messages[1].createdAt)
        restored.newChat()
        assertNull(requireNotNull(restored.send("Novo assunto", 4000L)).conversation.apiSessionId)
    }

    @Test fun roomValidationMatchesTheRemoteMinimumAndMaximum() {
        assertNotNull(roomValidationError("Banheiro", RoomIcon.Toilet, listOf("1"), listOf("1", "2"), 2..20))
        assertNull(roomValidationError("Banheiro", RoomIcon.Toilet, listOf("1", "2"), listOf("1", "2"), 2..20))
        val ids = (1..21).map(Int::toString)
        assertNotNull(roomValidationError("Banheiro", RoomIcon.Toilet, ids, ids, 2..20))
    }

    @Test fun transportAndContractFailuresAreNeverShownAsSuccessfulChatAnswers() {
        assertEquals(GenericErrorKind.Offline, apiErrorKind(UnknownHostException()))
        assertEquals(GenericErrorKind.Timeout, apiErrorKind(SocketTimeoutException()))
        assertEquals(GenericErrorKind.ContentUnavailable, apiErrorKind(ApiContractException("HTML da API")))
    }
}
