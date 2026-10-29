package com.quimia.android.presentation.screens.home

import com.quimia.android.data.auth.UserProfile
import com.quimia.android.data.home.*
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class HomeControllerTest {
    private val profile = UserProfile("u1", "Nome da API", "user@example.com")
    private fun item(id: Int) = HomeMixture(id, HomeProduct(1, "Produto real A"), HomeProduct(2, "Produto real B"),
        "nao_avaliado", consultadoEm = "2026-10-27T12:00:00.123456Z")
    private class Source(val profileValue: UserProfile) : HomeDataSource {
        var profileFailure: Exception? = null
        var historyFailure: Exception? = null
        val pages = mutableMapOf<Int, HomePage<HomeMixture>>()
        val requests = mutableListOf<Pair<String, Int>>()
        override suspend fun profile(userId: String): UserProfile { profileFailure?.let { throw it }; return profileValue }
        override suspend fun history(userId: String, page: Int, size: Int): HomePage<HomeMixture> {
            requests += userId to page
            historyFailure?.let { throw it }
            return pages[page] ?: HomePage(emptyList(), page, size, 0, 0)
        }
    }
    @Test fun emptyStateOnlyAppearsAfterSuccessfulApiResponses() = runBlocking {
        val controller = HomeController(Source(profile), "u1")
        assertTrue(controller.state.value.historyLoading)
        assertNull(controller.state.value.profile)
        controller.refresh()
        assertFalse(controller.state.value.historyLoading)
        assertTrue(controller.state.value.history.isEmpty())
        assertNull(controller.state.value.historyError)
        assertEquals(profile, controller.state.value.profile)
    }
    @Test fun failuresAreNotConvertedIntoASuccessfulEmptyHistory() = runBlocking {
        val source = Source(profile).apply { historyFailure = IOException() }
        val controller = HomeController(source, "u1")
        controller.refresh()
        assertEquals(profile, controller.state.value.profile)
        assertEquals(GenericErrorKind.Offline, controller.state.value.historyError)
        assertFalse(controller.state.value.historyLoading)
        source.historyFailure = null
        source.pages[0] = HomePage(listOf(item(1)), 0, 10, 1, 1)
        controller.refresh()
        assertNull(controller.state.value.historyError)
        assertEquals(listOf(1), controller.state.value.history.map { it.id })
    }
    @Test fun profileFailureDoesNotInventUserInfoOrPreventIndependentHistoryLoading() = runBlocking {
        val source = Source(profile).apply { profileFailure = IOException(); pages[0] = HomePage(listOf(item(1)), 0, 10, 1, 1) }
        val controller = HomeController(source, "u1")
        controller.refresh()
        assertNull(controller.state.value.profile)
        assertEquals(GenericErrorKind.Offline, controller.state.value.profileError)
        assertEquals(1, controller.state.value.history.size)
    }
    @Test fun paginationDeduplicatesRowsAndStopsOnTheLastPage() = runBlocking {
        val source = Source(profile).apply {
            pages[0] = HomePage(listOf(item(3), item(2)), 0, 10, 3, 2)
            pages[1] = HomePage(listOf(item(2), item(1)), 1, 10, 3, 2)
        }
        val controller = HomeController(source, "u1")
        controller.refresh(); controller.loadMore(); controller.loadMore()
        assertEquals(listOf(3, 2, 1), controller.state.value.history.map { it.id })
        assertNull(controller.state.value.nextPage)
        assertEquals(listOf("u1" to 0, "u1" to 1), source.requests)
    }
    @Test fun failedNextPageKeepsExistingRowsAndRetriesTheSamePage() = runBlocking {
        val source = Source(profile).apply { pages[0] = HomePage(listOf(item(2)), 0, 10, 2, 2) }
        val controller = HomeController(source, "u1")
        controller.refresh()
        source.historyFailure = SocketTimeoutException()
        controller.loadMore()
        assertEquals(listOf(2), controller.state.value.history.map { it.id })
        assertEquals(1, controller.state.value.nextPage)
        assertEquals(GenericErrorKind.Timeout, controller.state.value.moreError)
        source.historyFailure = null
        source.pages[1] = HomePage(listOf(item(1)), 1, 10, 2, 2)
        controller.loadMore()
        assertEquals(listOf(2, 1), controller.state.value.history.map { it.id })
        assertNull(controller.state.value.moreError)
    }
    @Test fun incompleteAndMalformedResponsesRemainErrorsInsteadOfFakeCards() = runBlocking {
        val source = Source(profile).apply { pages[0] = HomePage(listOf(item(1).copy(produtoA = null)), 0, 10, 1, 1) }
        val controller = HomeController(source, "u1")
        controller.refresh()
        assertTrue(controller.state.value.history.isEmpty())
        assertNotNull(controller.state.value.historyError)
        assertTrue(runCatching { validateHomePage(HomePage(listOf(item(1)), 2, 10, 1, 1), 0, 10) }.isFailure)
        assertTrue(runCatching { validateHomePage(HomePage(listOf(item(1).copy(consultadoEm = "invalid")), 0, 10, 1, 1), 0, 10) }.isFailure)
    }
    @Test fun dataFromAnotherAccountCannotBeDisplayed() = runBlocking {
        val controller = HomeController(Source(profile.copy(id = "other-user")), "u1")
        controller.refresh()
        assertNull(controller.state.value.profile)
        assertNotNull(controller.state.value.profileError)
        val another = HomeController(Source(profile.copy(id = "u2")), "u2")
        assertNull(another.state.value.profile)
        assertTrue(another.state.value.history.isEmpty())
    }
    @Test fun unassessedUnknownAndIncompatibleResultsAreNeverReclassifiedAsSafe() {
        assertEquals(HomeAssessment.Unassessed, item(1).assessment())
        assertEquals(HomeAssessment.Unknown, item(1).copy(resultado = "new_code").assessment())
        assertEquals(HomeAssessment.Incompatible, item(1).copy(resultado = "incompativel", severidade = "critica").assessment())
        assertEquals(HomeAssessment.Unknown, item(1).copy(resultado = "compativel", severidade = "critica").assessment())
        assertEquals(HomeAssessment.Compatible, item(1).copy(resultado = "compativel").assessment())
    }
    @Test fun datesRespectTheServerOffsetAndNanosecondsWithoutInventingTimestamps() {
        assertEquals(homeTimestamp("2026-10-27T12:00:00.123Z"), homeTimestamp("2026-10-27T09:00:00.123456789-03:00"))
        assertNull(homeTimestamp("invalid"))
        assertNull(homeTimestamp("2026-02-30T12:00:00Z"))
        assertNull(homePhotoUrl(null))
        assertNull(homePhotoUrl("file:///private/image.png"))
        assertNull(homePhotoUrl("content://image/1"))
        assertEquals("https://example.com/photo.png", homePhotoUrl("https://example.com/photo.png"))
    }
}
