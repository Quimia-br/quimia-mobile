package com.quimia.android.presentation.screens.mixtures

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import theme.QuimiaTheme

internal val previewMixtureIngredients: List<MixtureIngredient>
    get() = listOf(sampleCatalogProducts.first().mixtureIngredient(), sampleCatalogProducts.first { it.id == "omo" }.mixtureIngredient())

internal fun previewMixtureHistory(): List<MixtureResult> = listOf(MixtureRisk.Dangerous, MixtureRisk.Safe).mapIndexed { index, risk ->
    MixtureResult("figma-$index", previewMixtureIngredients, risk, System.currentTimeMillis(), listOf("15ml", "15ml"))
}

@Composable
private fun MixturePreview(count: Int = 0, picker: Boolean = false, substances: Boolean = false,
    pending: Boolean = false, risk: MixtureRisk? = null, history: Boolean = false, dark: Boolean = false, contrast: Boolean = false) {
    val ingredients = previewMixtureIngredients
    val slots = listOf(ingredients[0].takeIf { count > 0 },
        if (substances && !picker) MixtureIngredient("bicarbonato", "Bicarbonato de sódio", MixtureIngredientKind.Substance) else ingredients[1].takeIf { count > 1 })
    val result = risk?.let { MixtureResult("figma-result", ingredients, it, System.currentTimeMillis(),
        if (it == MixtureRisk.Caution) emptyList() else listOf("15ml", "15ml")) }
    QuimiaTheme(darkTheme = dark, highContrast = contrast) {
        val previewCatalog = sampleCatalogProducts.filter { it.id != "veja" }.sortedBy { if (it.id == "squeeze") 0 else if (it.id == "omo") 1 else 2 }
        val controller = remember { MixtureController(MixtureState(slots = slots, pendingId = if (pending) "figma-analysis" else null,
            result = result, history = if (history) previewMixtureHistory() else emptyList())) }
        MixturesScreen(controller, previewCatalog.map { it.id }, catalog = previewCatalog, onClose = {}, onOpenCatalog = {},
            substances = List(4) { MixtureIngredient(if (it == 0) "bicarbonato" else "bicarbonato-$it", "Bicarbonato de sódio", MixtureIngredientKind.Substance) },
            initialHistory = history, initialPickerSlot = if (picker) 1 else null, initialSubstances = substances)
    }
}

@Preview(name = "Figma · Seleção vazia", group = "Misturas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Compacto · Seleção vazia", group = "Misturas", widthDp = 320, heightDp = 640, showSystemUi = true)
@Composable
fun MixtureEmptyPreview() = MixturePreview()

@Preview(name = "Figma · Um produto", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureOneProductPreview() = MixturePreview(count = 1)

@Preview(name = "Figma · Dois produtos", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Preview(name = "Tablet · Dois produtos", group = "Misturas", widthDp = 800, heightDp = 1280, showSystemUi = true)
@Composable
fun MixtureTwoProductsPreview() = MixturePreview(count = 2)

@Preview(name = "Figma · Estante de produtos", group = "Misturas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
fun MixtureProductPickerPreview() = MixturePreview(count = 2, picker = true)

@Preview(name = "Figma · Estante de substâncias", group = "Misturas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
fun MixtureSubstancePickerPreview() = MixturePreview(picker = true, substances = true)

@Preview(name = "Figma · Produto e substância", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureProductSubstancePreview() = MixturePreview(count = 1, substances = true)

@Preview(name = "Figma · Analisando", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureAnalyzingPreview() = MixturePreview(count = 2, pending = true)

@Preview(name = "Figma · Resultado seguro", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureSafePreview() = MixturePreview(count = 2, risk = MixtureRisk.Safe)

@Preview(name = "Figma · Resultado cuidado", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureCautionPreview() = MixturePreview(count = 2, risk = MixtureRisk.Caution)

@Preview(name = "Figma · Resultado perigoso", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureDangerousPreview() = MixturePreview(count = 2, risk = MixtureRisk.Dangerous)

@Preview(name = "Figma · Resultado perigo extremo", group = "Misturas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
fun MixtureExtremePreview() = MixturePreview(count = 2, risk = MixtureRisk.Extreme)

@Preview(name = "GitHub · Histórico de misturas", group = "GitHub Screens", widthDp = 360, heightDp = 937, showSystemUi = true)
@Composable
fun MixtureHistoryPreview() = MixturePreview(history = true)

@Preview(name = "Escuro · Seleção", group = "Misturas", widthDp = 360, heightDp = 840, showSystemUi = true)
@Composable
fun MixtureDarkPreview() = MixturePreview(count = 2, dark = true)

@Preview(name = "Contraste · Estante", group = "Misturas", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
fun MixtureContrastPreview() = MixturePreview(count = 2, picker = true, dark = true, contrast = true)
