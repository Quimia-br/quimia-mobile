package com.quimia.android.presentation.screens.mixtures

import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class MixtureControllerTest {
    private val first = MixtureIngredient("one", "Produto um", MixtureIngredientKind.Product)
    private val second = MixtureIngredient("two", "Produto dois", MixtureIngredientKind.Product)
    private fun ready() = MixtureController().apply { select(0, first); select(1, second) }
    private fun answer(request: MixtureRequest, time: Long = 100L) = MixtureReply.Evaluated(
        MixtureResult(request.id, request.ingredients, MixtureRisk.Caution, time, demonstration = true))

    @Test fun cannotAnalyzeAnEmptyOrRepeatedSelection() {
        val controller = MixtureController()
        assertNull(controller.begin())
        assertTrue(controller.select(0, first))
        assertFalse(controller.select(1, first))
        assertNull(controller.begin())
    }
    @Test fun namesDoNotConfuseProductsAndSubstances() {
        val controller = MixtureController()
        controller.select(0, first)
        assertTrue(controller.select(1, first.copy(kind = MixtureIngredientKind.Substance)))
        assertNotNull(controller.begin())
    }
    @Test fun ignoresLateResponseAfterChangingTheDraft() {
        val controller = ready()
        val request = requireNotNull(controller.begin())
        controller.cancel()
        controller.startWith(second)
        assertFalse(controller.complete(request, answer(request)))
        assertNull(controller.state.result)
    }
    @Test fun disallowsDuplicateRequestsAndEditsDuringAnalysis() {
        val controller = ready()
        assertNotNull(controller.begin())
        assertNull(controller.begin())
        controller.remove(0)
        assertEquals(first, controller.state.slots[0])
        assertFalse(controller.select(0, second))
    }
    @Test fun neverAcceptsAResultForAnotherCombination() {
        val controller = ready()
        val request = requireNotNull(controller.begin())
        val wrong = answer(request).result.copy(ingredients = listOf(second, first))
        assertFalse(controller.complete(request, MixtureReply.Evaluated(wrong)))
        assertNull(controller.state.result)
        assertTrue(controller.state.history.isEmpty())
    }
    @Test fun historyChangesOnlyAfterExplicitSaveAndDoesNotDuplicate() {
        val controller = ready()
        val request = requireNotNull(controller.begin())
        assertTrue(controller.complete(request, answer(request)))
        assertTrue(controller.state.history.isEmpty())
        assertTrue(controller.saveResult())
        controller.openResult(request.id)
        controller.saveResult()
        assertEquals(1, controller.state.history.size)
        assertTrue(controller.state.history.single().demonstration)
    }
    @Test fun historyIsNewestFirstAndSurvivesRestoration() {
        val controller = ready()
        repeat(2) { index ->
            val request = requireNotNull(controller.begin())
            controller.complete(request, answer(request, 100L + index))
            controller.saveResult()
        }
        val restored = restoreMixtureState(Json.encodeToString(controller.state))
        assertEquals(listOf(101L, 100L), restored.history.map { it.createdAt })
    }
    @Test fun restorationDoesNotResumeAnInterruptedAnalysis() {
        val controller = ready()
        controller.begin()
        assertNull(restoreMixtureState(Json.encodeToString(controller.state)).pendingId)
        assertEquals(MixtureState(), restoreMixtureState("broken"))
        assertEquals(MixtureState(), restoreMixtureState(Json.encodeToString(MixtureState(slots = listOf(first)))))
    }
    @Test fun missingAnalyzerDoesNotInventASafeResult() = runBlocking {
        val request = requireNotNull(ready().begin())
        assertEquals(MixtureReply.Failed(GenericErrorKind.ServiceUnavailable), analyzeMixture(null, request))
    }
    @Test fun cancellationPropagatesInsteadOfGeneratingAnErrorOrResult() = runBlocking {
        try {
            analyzeMixture({ throw CancellationException() }, requireNotNull(ready().begin()))
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
    @Test fun todayAndYesterdayUseTheDeviceTimezone() {
        val zone = TimeZone.getTimeZone("America/Sao_Paulo")
        val now = 1_792_551_600_000L
        assertEquals("Hoje", mixtureDateLabel(now, now, zone))
        assertEquals("Ontem", mixtureDateLabel(now - 86_400_000L, now, zone))
    }
}
