package com.quimia.android.presentation.screens.mixtures

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.quimia.android.presentation.components.DestinationLoadingHost
import com.quimia.android.presentation.components.ReportDestinationLoading
import com.quimia.android.presentation.screens.auth.registration.Cadastro1Screen
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.rooms.RoomForm
import com.quimia.android.presentation.screens.rooms.RoomIcon
import kotlinx.coroutines.CompletableDeferred
import motion.LocalQuimiaMotionEnabled
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import theme.QuimiaTheme
import theme.quimiaColorTokens
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RefinedFlowInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val products = sampleCatalogProducts.take(3)
    private val substance = MixtureIngredient("s1", "Bicarbonato de sódio", MixtureIngredientKind.Substance)

    private fun content(animate: Boolean = false, body: @Composable () -> Unit) {
        compose.setContent { QuimiaTheme(false) {
            Surface(Modifier.fillMaxSize(), color = quimiaColorTokens().surfaceBackground) {
                CompositionLocalProvider(LocalQuimiaMotionEnabled provides animate, content = body)
            }
        } }
    }
    private fun capture(name: String) = compose.runOnIdle {
        val view = compose.activity.window.decorView
        val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(bitmap))
        val directory = File("build/reports/flow-review").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun roomSaveIsVisibleBeforeScrollingAndKeepsItsPosition() {
        content { RoomForm("Cozinha", RoomIcon.Refrigerator, products.map { it.id }, products, null,
            {}, {}, {}, {}, {}, {}, submitLabel = "Salvar") }
        val save = compose.onNodeWithText("Salvar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        compose.onNodeWithContentDescription("Selecionar ${products.last().name}").performScrollTo()
        val after = compose.onNodeWithText("Salvar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertEquals(save.top, after.top, .1f)
        assertEquals(save.bottom, after.bottom, .1f)
        capture("room-fixed-save")
    }

    @Test @Config(sdk = [34], qualifiers = "w360dp-h640dp")
    fun registrationContinueRemainsVisibleOnSmallPhone() {
        content { Cadastro1Screen() }
        val before = compose.onNodeWithText("Continuar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        compose.onNodeWithText("Crie uma senha").performScrollTo()
        val after = compose.onNodeWithText("Continuar").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertEquals(before.top, after.top, .1f)
        capture("registration-fixed-footer")
    }

    @Test fun pickerShowsBothTabsAndSubstanceCanBeUsedWithoutBeingConfusedWithAProduct() {
        var chosen: MixtureIngredient? = null
        content { Surface(color = quimiaColorTokens().secondary) {
            MixturePickerContent(products, listOf(substance, substance.copy(id = "s2", name = "Ácido cítrico"),
                substance.copy(id = "s3", name = "Carbonato de sódio"), substance.copy(id = "s4", name = "Água")),
                null, null, {}, { chosen = it }, {}, modifier = Modifier.fillMaxWidth().height(515.dp))
        } }
        compose.onNodeWithText("Produtos").assertIsSelected()
        compose.onNodeWithText("Substâncias").performClick().assertIsSelected()
        compose.onNodeWithText(substance.name).performClick()
        compose.onNodeWithText("Usar").assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(substance, chosen) }
        capture("substances-picker")
    }

    @Test fun substancesHaveTheirOwnErrorAndRetryWithoutHidingAvailableProducts() {
        var retries = 0
        content { MixturePickerContent(products, emptyList(), null, null, {}, {}, {}, Modifier.height(650.dp),
            substancesError = "Falha ao buscar substâncias", onRetrySubstances = { retries++ }) }
        compose.onNodeWithText(products.first().name).assertExists()
        compose.onNodeWithText("Substâncias").performClick()
        compose.onNodeWithText("Falha ao buscar substâncias").assertIsDisplayed()
        compose.onNodeWithText("Tentar novamente").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
        compose.onNodeWithText("Produtos").performClick()
        compose.onNodeWithText(products.first().name).assertExists()
    }

    @Test fun unsupportedSubstanceAnalysisKeepsSelectionAndNeverCallsTheProductEndpoint() {
        val controller = MixtureController(MixtureState(slots = listOf(products.first().mixtureIngredient(), substance)))
        var requests = 0
        content { MixturesScreen(controller, products.map { it.id }, {}, {}, catalog = products,
            substances = listOf(substance), supportsSubstanceAnalysis = false, analyzer = { requests++; MixtureReply.Recorded },
            remoteHistoryContent = {}) }
        compose.onNodeWithText("Misturar").assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText("A análise de substâncias ainda não está disponível. Para analisar agora, selecione dois produtos.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, requests); assertEquals(substance, controller.state.slots[1]) }
    }

    @Test fun internalLoadingCoversThePageImmediatelyAndNeverReturnsForAComponent() {
        var internal by mutableStateOf(false)
        var pending by mutableStateOf(false)
        content { DestinationLoadingHost("rooms", "user", false, navigationVisible = true) {
            ReportDestinationLoading("rooms", pending, navigationVisible = !internal, page = internal)
            Text(if (internal) "Detalhe do cômodo" else "Lista de cômodos")
        } }
        compose.onNodeWithText("Lista de cômodos").assertIsDisplayed()
        compose.runOnIdle { internal = true; pending = true }
        compose.onNodeWithContentDescription("Carregando").assertIsDisplayed()
        compose.onNodeWithText("Detalhe do cômodo").assertDoesNotExist()
        compose.runOnIdle { pending = false }
        compose.onNodeWithText("Detalhe do cômodo").assertIsDisplayed()
        compose.onNodeWithContentDescription("Carregando").assertDoesNotExist()
        compose.runOnIdle { pending = true }
        compose.onNodeWithContentDescription("Carregando").assertDoesNotExist()
    }

    @Test fun analysisAnimationStartsImmediatelyAndWaitsForTheRealResponse() {
        val controller = MixtureController(MixtureState(slots = products.take(2).map { it.mixtureIngredient() }))
        val response = CompletableDeferred<MixtureReply>()
        var requests = 0; var recorded = 0
        compose.mainClock.autoAdvance = false
        content(animate = true) { MixturesScreen(controller, products.map { it.id }, {}, {}, catalog = products,
            analyzer = { requests++; response.await() }, onAnalysisRecorded = { recorded++ }) }
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("Misturar").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithContentDescription("Analisando combinação").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, requests); assertEquals(0, recorded) }
        compose.mainClock.advanceTimeBy(2000)
        compose.onNodeWithContentDescription("Analisando combinação").assertIsDisplayed()
        capture("analysis-motion")
        compose.runOnIdle { response.complete(MixtureReply.Recorded) }
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, recorded); assertNull(controller.state.pendingId) }
    }
}
