package com.quimia.android.presentation.screens.rooms

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.quimia.android.presentation.screens.catalog.CatalogScreen
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import feedback.LocalQuimiaSound
import feedback.QuimiaSound
import motion.LocalQuimiaMotionEnabled
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import theme.QuimiaTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProductInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val product = sampleCatalogProducts.first()
    private val sounds = mutableListOf<QuimiaSound>()

    private fun content(block: @Composable () -> Unit) {
        compose.setContent { QuimiaTheme(false) {
            CompositionLocalProvider(LocalQuimiaSound provides { sounds += it }, LocalQuimiaMotionEnabled provides false) { block() }
        } }
    }

    @Test fun selectingAndDeselectingProductEmitsOneCuePerActionAndDisabledCardsAreSilent() {
        var checked by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        content { RoomProductCard(product, selectable = true, checked = checked, enabled = enabled, onCheckedChange = { checked = it }) }
        val card = compose.onNodeWithContentDescription("Selecionar ${product.name}")
        card.performClick().assertIsOn()
        card.performClick().assertIsOff()
        compose.runOnIdle { assertEquals(listOf(QuimiaSound.Selection, QuimiaSound.Selection), sounds); enabled = false }
        card.assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertFalse(checked); assertEquals(2, sounds.size) }
    }

    @Test fun tappingRoomProductOrItsMenuOpensActionsWithOneTapCue() {
        var actions = 0
        content { RoomProductCard(product, onActions = { actions++ }) }
        compose.onNodeWithText(product.name).performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, actions); assertEquals(listOf(QuimiaSound.Tap), sounds) }
        compose.onNodeWithContentDescription("Ações de ${product.name}").performClick()
        compose.runOnIdle { assertEquals(2, actions); assertEquals(listOf(QuimiaSound.Tap, QuimiaSound.Tap), sounds) }
    }

    @Test fun catalogLongPressAndSelectionTapUseSelectionSoundWithoutDuplicateTap() {
        content { CatalogScreen(products = listOf(product), initialBannerVisible = false, showFab = false) }
        compose.onNodeWithText(product.name).performTouchInput { longClick() }
        compose.runOnIdle { assertEquals(listOf(QuimiaSound.Selection), sounds) }
        compose.onNodeWithText(product.name).performTouchInput { click() }
        compose.runOnIdle { assertEquals(listOf(QuimiaSound.Selection, QuimiaSound.Selection), sounds) }
    }

    @Test fun savedCatalogProductDoesNotEmitSelectionSoundOnLongPress() {
        content { CatalogScreen(products = listOf(product), initialSavedProductIds = listOf(product.id), initialBannerVisible = false, showFab = false) }
        compose.onNodeWithText(product.name).performTouchInput { longClick() }
        compose.runOnIdle { assertTrue(sounds.isEmpty()) }
    }

    @Test fun catalogCardAndInformationButtonEachOpenDetailsWithOneTapCue() {
        var opened = 0
        content { CatalogScreen(products = listOf(product), initialBannerVisible = false, showFab = false,
            onProductDetails = { assertEquals(product.id, it.id); opened++ }) }
        compose.onNodeWithText(product.name).performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, opened); assertEquals(listOf(QuimiaSound.Tap), sounds) }
        compose.onNodeWithContentDescription("Informações sobre ${product.name}").performClick()
        compose.runOnIdle { assertEquals(2, opened); assertEquals(listOf(QuimiaSound.Tap, QuimiaSound.Tap), sounds) }
    }

    @Test fun roomMenuOpensFullPageEditWithPreselectedProductsAndPersistsChanges() {
        val catalog = sampleCatalogProducts.take(3)
        var rooms by mutableStateOf(listOf(QuimiaRoom("kitchen", "Cozinha", RoomIcon.Refrigerator, catalog.take(2).map { it.id })))
        content { RoomsScreen(rooms, { rooms = it }, catalog.map { it.id }, initialPage = RoomPage.Detail,
            initialRoomId = "kitchen", catalog = catalog) }
        compose.onNodeWithContentDescription("Ações do cômodo").performClick()
        compose.onNodeWithText("Editar Cozinha").assertIsDisplayed()
        compose.onNodeWithText("Adicionar produto").assertIsDisplayed()
        compose.onNodeWithText("Excluir cômodo").assertIsDisplayed()
        compose.onNodeWithText("Editar cômodo").performClick()
        compose.onNodeWithText("Editar as informações do cômodo").assertIsDisplayed()
        compose.onNodeWithContentDescription("Selecionar ${catalog[1].name}").performScrollTo().assertIsOn().performClick().assertIsOff()
        compose.onNodeWithText("Salvar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(listOf(catalog.first().id), rooms.single().productIds) }
        compose.onNodeWithContentDescription("Ações do cômodo").assertIsDisplayed()
    }

    @Test fun roomDetailAndEditRenderOnPhone() {
        var rooms by mutableStateOf(listOf(QuimiaRoom("kitchen", "Cozinha", RoomIcon.Refrigerator, listOf(product.id))))
        content { RoomsScreen(rooms, { rooms = it }, sampleCatalogProducts.map { it.id }, initialPage = RoomPage.Detail,
            initialRoomId = "kitchen", catalog = sampleCatalogProducts) }
        fun capture(name: String, dialog: Boolean = false) {
            compose.runOnIdle {
                val decor = if (dialog) requireNotNull(org.robolectric.shadows.ShadowDialog.getLatestDialog().window).decorView
                    else compose.activity.window.decorView
                val bitmap = android.graphics.Bitmap.createBitmap(decor.width, decor.height, android.graphics.Bitmap.Config.ARGB_8888)
                decor.draw(android.graphics.Canvas(bitmap))
                val directory = File("build/reports/rooms-review").apply { mkdirs() }
                File(directory, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
        compose.onNodeWithContentDescription("Ações do cômodo").assertIsDisplayed()
        capture("detail-phone")
        compose.onNodeWithContentDescription("Ações do cômodo").performClick()
        compose.onNodeWithText("Editar cômodo").assertIsDisplayed()
        capture("menu-phone", dialog = true)
        compose.onNodeWithText("Editar cômodo").performClick()
        compose.onNodeWithText("Editar as informações do cômodo").assertIsDisplayed()
        capture("edit-phone")
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Ações do cômodo").performClick()
        compose.onNodeWithText("Excluir cômodo").performClick()
        compose.onNodeWithText("Essa ação não poderá ser desfeita").assertIsDisplayed()
        capture("delete-phone", dialog = true)
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithContentDescription("Ações do cômodo").performClick()
        compose.onNodeWithText("Adicionar produto").performClick()
        compose.onNodeWithText("Adicionar").assertIsDisplayed()
        capture("add-phone", dialog = true)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w800dp-h1280dp")
    fun editProductsStaySideBySideOnTabletAndSaveRemainsReachable() {
        val catalog = sampleCatalogProducts.take(3)
        val room = QuimiaRoom("kitchen", "Cozinha", RoomIcon.Refrigerator, catalog.take(2).map { it.id })
        content { RoomsScreen(listOf(room), {}, catalog.map { it.id }, initialPage = RoomPage.Editing,
            initialRoomId = "kitchen", catalog = catalog, initialDraftName = room.name, initialDraftIcon = room.icon,
            initialDraftProductIds = room.productIds) }
        val first = compose.onNodeWithContentDescription("Selecionar ${catalog[0].name}").assertIsOn().fetchSemanticsNode().boundsInRoot
        val second = compose.onNodeWithContentDescription("Selecionar ${catalog[1].name}").assertIsOn().fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top, 1f)
        assertTrue(second.left > first.right)
        compose.onNodeWithText("Salvar").assertIsDisplayed().assertIsNotEnabled()
    }
}
