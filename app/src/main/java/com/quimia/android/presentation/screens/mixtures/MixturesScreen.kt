package com.quimia.android.presentation.screens.mixtures

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Atom
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.errors.GenericErrorAction
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.errors.GenericErrorScreen
import com.quimia.android.presentation.screens.shelf.sampleShelfSubstances
import kotlinx.coroutines.launch
import listItem.QuimiaListItem
import nav.QuimiaNav
import shapeIcon.QuimiaShapeIcon
import states.QuimiaEmptyState
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
fun MixturesScreen(
    controller: MixtureController,
    savedProductIds: List<String>,
    onClose: () -> Unit,
    onOpenCatalog: () -> Unit,
    catalog: List<CatalogProduct> = sampleCatalogProducts,
    substances: List<MixtureIngredient> = sampleShelfSubstances.map { MixtureIngredient(it.id, it.name, MixtureIngredientKind.Substance) },
    analyzer: MixtureAnalyzer? = null,
    initialHistory: Boolean = false,
    initialPickerSlot: Int? = null,
    initialSubstances: Boolean = false,
    onOpenHistoryConsumed: () -> Unit = {},
    historyProductId: String? = null,
    demonstration: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val preview = LocalInspectionMode.current
    var historyVisible by rememberSaveable { mutableStateOf(initialHistory) }
    var pickerSlot by rememberSaveable { mutableStateOf(initialPickerSlot) }
    var failure by rememberSaveable { mutableStateOf<GenericErrorKind?>(null) }
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }
    val state = controller.state
    val products = savedProductIds.distinct().mapNotNull { id -> catalog.firstOrNull { it.id == id } }
    val history = state.history.filter { result -> historyProductId == null || result.ingredients.any { it.id == historyProductId } }
    var analysisJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun analyze() {
        val request = controller.begin() ?: return
        analysisJob = scope.launch {
            val reply = analyzeMixture(analyzer, request)
            val accepted = controller.complete(request, reply)
            if (!accepted && controller.state.pendingId == null) failure = (reply as? MixtureReply.Failed)?.kind ?: GenericErrorKind.ContentUnavailable
        }
    }
    fun close() {
        analysisJob?.cancel(); controller.cancel()
        onClose()
    }
    LaunchedEffect(initialHistory, historyProductId) {
        if (initialHistory) { historyVisible = true; onOpenHistoryConsumed() }
    }
    DisposableEffect(controller) { onDispose { controller.cancel() } }
    BackHandler {
        when {
            failure != null -> failure = null
            pickerSlot != null -> pickerSlot = null
            state.result != null -> controller.closeResult()
            historyVisible -> historyVisible = false
            else -> close()
        }
    }
    if (failure != null) {
        GenericErrorScreen(requireNotNull(failure), onClose = { failure = null }, onAction = { action ->
            failure = null
            if (action == GenericErrorAction.Retry) analyze() else close()
        })
        return
    }

    androidx.compose.material3.Surface(Modifier.fillMaxSize(), color = tokens.surfaceBackground) {
      Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(276.dp).background(Brush.verticalGradient(
            listOf(tokens.brandSecondary.copy(alpha = .86f), tokens.surfaceBackground.copy(alpha = .72f), tokens.surfaceBackground))))
        LazyColumn(Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            contentPadding = PaddingValues(top = if (preview) 38.dp else (62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp), bottom = 24.dp)) {
            item { QuimiaNav(showBackButton = true, showCloseButton = false, buttonColor = tokens.surfaceBase,
                onBackClick = { if (historyVisible) historyVisible = false else close() }) }
            item {
                Column(Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(when { historyVisible -> "Histórico de misturas"; demonstration -> "Misturas · demonstração"; else -> "Misturas" },
                        style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
                    Text(if (historyVisible) "Veja as misturas que você escolheu guardar no histórico" else "Selecione dois produtos da estante e veja o risco da combinação entre eles",
                        style = QuimiaTextStyles.medium16, color = tokens.textSecondary)
                }
            }
            if (historyVisible) {
                if (history.isEmpty()) item {
                    QuimiaEmptyState(icon = Lucide.Atom, title = "Você ainda não salvou\nnenhuma mistura", buttonLabel = "Misturar",
                        onButtonClick = { historyVisible = false }, fillAvailableSpace = false)
                }
                items(history, key = { it.id }) { result ->
                    MixtureHistoryCard(result, onClick = { controller.openResult(result.id) }, Modifier.padding(horizontal = 24.dp).padding(bottom = 20.dp))
                }
            } else {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        state.slots.forEachIndexed { index, ingredient ->
                            MixtureSlot(ingredient, index, state.pendingId == null,
                                onChoose = { pickerSlot = index }, onRemove = { controller.remove(index) }, modifier = Modifier.weight(1f),
                                filledHeight = state.slots.any { it != null })
                        }
                    }
                }
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = if (state.slots.any { it != null }) 0.dp else 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        if (state.pendingId != null) Row(Modifier.fillMaxWidth().height(43.dp)
                            .background(tokens.primaryDisabled, androidx.compose.foundation.shape.CircleShape)
                            .semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "Analisando mistura" },
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = tokens.foregroundDisabled, strokeWidth = 2.dp)
                        } else QuimiaButton(text = "Misturar", enabled = state.canAnalyze, modifier = Modifier.fillMaxWidth().height(43.dp),
                            textStyle = QuimiaTextStyles.medium16, iconSize = 0, espacamento = 0, onClick = ::analyze)
                        QuimiaListItem(title = "Histórico de misturas", subtitle = "Suas últimas misturas salvas", containerColor = tokens.surfaceBase,
                            horizontalSpacing = 20.dp, leadingContent = { QuimiaShapeIcon(size = 50.dp, icon = Lucide.Clock, containerColor = tokens.surfaceBackground) },
                            onClick = { historyVisible = true })
                    }
                }
            }
        }
        SnackbarHost(snackbars, Modifier.align(Alignment.BottomCenter).padding(24.dp))
        pickerSlot?.let { slot ->
            MixturePicker(products, substances, state.slots[1 - slot], state.slots[slot], onDismiss = { pickerSlot = null },
                onUse = { if (controller.select(slot, it)) pickerSlot = null },
                onOpenCatalog = { pickerSlot = null; onOpenCatalog() }, initialSubstances = initialSubstances)
        }
        state.result?.let { result -> MixtureResultSheet(result, onClose = controller::closeResult,
            saved = state.history.any { it.id == result.id }, onSave = {
                if (controller.saveResult()) scope.launch { snackbars.showSnackbar("Mistura salva no histórico") }
            }) }
      }
    }
}
