package com.quimia.android.presentation.screens.rooms

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.quimia.android.presentation.preview.QuimiaScreenPreviews
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import theme.QuimiaTheme

internal val roomPreviewCatalog = (1..4).map { index -> sampleCatalogProducts.first().copy(id = if (index == 1) "squeeze" else "squeeze-$index") }

@QuimiaScreenPreviews
@Preview(name = "Figma · Cômodos", group = "Figma", widthDp = 360, heightDp = 975, showSystemUi = true)
@Preview(name = "GitHub · Cômodos", group = "GitHub Screens", widthDp = 360, heightDp = 975, showSystemUi = true)
@Preview(name = "Cômodos · tablet paisagem", group = "Tablet QA", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Composable
private fun RoomsPreview() {
    QuimiaTheme(false) {
        var rooms by remember { mutableStateOf(previewRooms) }
        RoomsScreen(rooms, { rooms = it }, roomPreviewCatalog.map { it.id }, catalog = roomPreviewCatalog)
    }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Cadastrar cômodo", group = "Figma", widthDp = 360, heightDp = 1433, showSystemUi = true)
@Composable
private fun RoomFormPreview() {
    QuimiaTheme(false) { RoomsScreen(emptyList(), {}, roomPreviewCatalog.map { it.id }, initialPage = RoomPage.Create, catalog = roomPreviewCatalog) }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Cômodo selecionado", group = "Figma", widthDp = 360, heightDp = 1433, showSystemUi = true)
@Composable
private fun RoomSelectedFormPreview() {
    QuimiaTheme(false) { RoomsScreen(emptyList(), {}, roomPreviewCatalog.map { it.id }, initialPage = RoomPage.Create, catalog = roomPreviewCatalog,
        initialDraftName = "Banheiro", initialDraftIcon = RoomIcon.Toilet, initialDraftProductIds = listOf("squeeze", "squeeze-3")) }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Cômodo crítico", group = "Figma", widthDp = 360, heightDp = 1094, showSystemUi = true)
@Composable
private fun RoomCriticalPreview() {
    QuimiaTheme(false) { RoomsScreen(previewRooms, {}, roomPreviewCatalog.map { it.id }, initialPage = RoomPage.Detail,
        initialRoomId = "bathroom", catalog = roomPreviewCatalog) }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Cômodo seguro · demonstração", group = "Figma", widthDp = 360, heightDp = 1094, showSystemUi = true)
@Composable
private fun RoomSafePreview() {
    QuimiaTheme(false) { RoomsScreen(previewRooms, {}, roomPreviewCatalog.map { it.id }, initialPage = RoomPage.Detail,
        initialRoomId = "kitchen", catalog = roomPreviewCatalog) }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Ações do produto no cômodo", group = "Figma", widthDp = 360, heightDp = 1094, showSystemUi = true)
@Composable
private fun RoomActionsPreview() {
    QuimiaTheme(false) { RoomsScreen(previewRooms, {}, roomPreviewCatalog.map { it.id }, initialPage = RoomPage.Detail,
        initialRoomId = "bathroom", catalog = roomPreviewCatalog, previewActionProductId = "squeeze") }
}

@QuimiaScreenPreviews
@Preview(name = "Figma · Criando cômodo", group = "Figma", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun RoomCreatingPreview() { QuimiaTheme(false) { RoomsScreen(emptyList(), {}, emptyList(), initialPage = RoomPage.Saving) } }

@QuimiaScreenPreviews
@Preview(name = "Figma · Cômodo criado", group = "Figma", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun RoomSuccessPreview() { QuimiaTheme(false) { RoomsScreen(previewRooms, {}, emptyList(), initialPage = RoomPage.Success, initialRoomId = "bathroom") } }

@QuimiaScreenPreviews
@Preview(name = "Figma · Erro ao criar cômodo", group = "Figma", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun RoomErrorPreview() { QuimiaTheme(false) { RoomsScreen(emptyList(), {}, emptyList(), initialPage = RoomPage.Error) } }

@QuimiaScreenPreviews
@Preview(name = "Cômodos · texto ampliado", group = "Acessibilidade", widthDp = 320, heightDp = 720, fontScale = 1.5f)
@Composable
private fun RoomsEmptyPreview() { QuimiaTheme(false) { RoomsScreen(emptyList(), {}, emptyList()) } }
