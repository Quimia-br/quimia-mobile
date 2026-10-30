package com.quimia.android.presentation.screens.shelf

import motion.quimiaContent
import motion.QuimiaExpand
import motion.quimiaClickable
import motion.quimiaSelectionFeedback
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import buttons.QuimiaFAB
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import card.QuimiaCard
import com.composables.icons.lucide.Bath
import com.composables.icons.lucide.Atom
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings2
import com.composables.icons.lucide.SoapDispenserDroplet
import com.composables.icons.lucide.X
import com.quimia.android.presentation.screens.catalog.CatalogImage
import com.quimia.android.presentation.screens.catalog.CatalogDescriptionCard
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.R
import chip.QuimiaChip
import com.quimia.android.presentation.screens.catalog.sampleCatalogProducts
import com.quimia.android.presentation.screens.rooms.QuimiaRoom
import com.quimia.android.presentation.screens.rooms.roomsForProduct
import header.QuimiaSearchBar
import com.quimia.android.presentation.components.rememberVoiceInput
import image.QuimiaImage
import listItem.QuimiaListItem
import menuBar.QuimiaMenuBar
import menuBar.QuimiaMenuBarBackdrop
import menuBar.QuimiaMenuBarBottomSpacing
import menuBar.QuimiaMenuBarHeight
import buttons.QuimiaFABSize
import states.QuimiaEmptyState
import theme.QuimiaTheme
import theme.Typography
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import title.QuimiaSectionTitle
import kotlinx.coroutines.delay

private enum class ShelfTab(val label: String) { Products("Produtos"), Substances("Substâncias") }
internal val ShelfFavoriteColor = Color(0xFFFF93D7)
internal val ShelfBodyStyle: androidx.compose.ui.text.TextStyle
    @Composable get() = QuimiaTextStyles.medium15

@Composable
internal fun ShelfScreen(
    savedIds: List<String>,
    onSavedIdsChange: (List<String>) -> Unit,
    onMenuItemSelected: (Int) -> Unit = {},
    onAiClick: (() -> Unit)? = null,
    onAddProduct: () -> Unit = {},
    favoriteIds: List<String> = emptyList(),
    onFavoriteIdsChange: (List<String>) -> Unit = {},
    onMixProduct: (String) -> Unit = {},
    onViewMixtures: (String) -> Unit = {},
    onCreateRoom: (String) -> Unit = {},
    initialSubstancesTab: Boolean = false,
    initialFiltersVisible: Boolean = false,
    initialFavoriteSubstanceIds: List<String> = emptyList(),
    rooms: List<QuimiaRoom> = emptyList(),
    onOpenRoom: (String) -> Unit = {},
    apiProducts: List<CatalogProduct>? = null,
    apiSubstances: List<ShelfSubstance>? = null,
    initialQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    onTabChange: (Boolean) -> Unit = {},
    onSortChange: (ShelfSort?) -> Unit = {},
    onApiFavorite: ((String, Boolean) -> Unit)? = null,
    onUnavailable: (String) -> Unit = {},
    onApiActions: ((CatalogProduct) -> Unit)? = null,
    onApiRoom: ((String) -> Unit)? = null,
    onApiExpand: (String) -> Unit = {},
    onSubstanceMix: ((String) -> Unit)? = null,
    onSubstanceMixtures: ((String) -> Unit)? = null,
    showEmpty: Boolean = true,
    statusContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    protectionContent: (@Composable (String) -> Unit)? = null,
    externalOverlay: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    var tab by rememberSaveable { mutableStateOf(if (initialSubstancesTab) ShelfTab.Substances else ShelfTab.Products) }
    var query by rememberSaveable(initialQuery) { mutableStateOf(initialQuery) }
    fun changeQuery(value: String) { query = value.take(100); onQueryChange(query) }
    val voiceInput = rememberVoiceInput { changeQuery(it) }
    var sort by rememberSaveable { mutableStateOf<ShelfSort?>(ShelfSort.Alphabetical) }
    var filtersVisible by rememberSaveable { mutableStateOf(initialFiltersVisible) }
    var favoriteSubstanceIds by rememberSaveable { mutableStateOf(initialFavoriteSubstanceIds) }
    var expandedSubstanceIds by rememberSaveable { mutableStateOf(sampleShelfSubstances.map { it.id }) }
    var expandedIds by rememberSaveable { mutableStateOf(savedIds.take(1)) }
    var actionId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var removeId by rememberSaveable { mutableStateOf<String?>(null) }
    var roomId by rememberSaveable { mutableStateOf<String?>(null) }
    var successVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(successVisible) {
        if (successVisible) { delay(4_000); successVisible = false }
    }

    val products = remember(apiProducts, savedIds, query, sort, favoriteIds) {
        apiProducts ?: filterShelfProducts(sampleCatalogProducts, savedIds, favoriteIds, query, sort)
    }
    val substances = apiSubstances ?: remember(query, sort, favoriteSubstanceIds) {
        val filtered = sampleShelfSubstances.filter {
            it.name.contains(query.trim(), true) && (sort != ShelfSort.Favorites || it.id in favoriteSubstanceIds)
        }
        when (sort) {
            ShelfSort.Alphabetical -> filtered.sortedBy { it.name }
            ShelfSort.Recent -> filtered.reversed()
            else -> filtered
        }
    }
    val overlayVisible = externalOverlay || actionId != null || detailId != null || removeId != null || roomId != null

    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)) {
        Box(Modifier.fillMaxWidth().height(276.dp).background(
            Brush.verticalGradient(listOf(tokens.brandSecondary.copy(alpha = 0.86f), tokens.surfaceBackground.copy(alpha = 0.72f), tokens.surfaceBackground))
        ))
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding().quimiaContent(),
            contentPadding = PaddingValues(
                top = (62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp),
                bottom = QuimiaMenuBarHeight + QuimiaMenuBarBottomSpacing + QuimiaFABSize + 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (successVisible) item {
                ShelfSuccessBanner(onDismiss = { successVisible = false })
            }
            item {
                QuimiaSectionTitle(
                    title = "Minha estante", titleSize = 24.sp,
                    textStyle = QuimiaTextStyles.medium24,
                    horizontalPadding = 32.dp, verticalPadding = 32.dp,
                )
            }
            item {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShelfTab.entries.forEach { item ->
                        QuimiaButton(
                            modifier = Modifier.weight(1f).height(44.dp), text = item.label,
                            textStyle = ShelfBodyStyle, iconSize = 20, espacamento = 10, centerContent = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            iconLeftVector = if (item == ShelfTab.Products) SoapDispenserDroplet else null,
                            iconLeft = if (item == ShelfTab.Substances) R.drawable.figma_bubbles else null,
                            containerColor = if (tab == item) tokens.surfaceBase else Color.Transparent,
                            textColor = tokens.foregroundPrimary, onClick = { tab = item; onTabChange(item == ShelfTab.Substances) },
                        )
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuimiaSearchBar(
                        value = query, onValueChange = { changeQuery(it) }, modifier = Modifier.weight(1f).height(58.dp),
                        placeholder = "Pesquisar", icon = Lucide.Mic, iconDescription = "Pesquisar por voz",
                        onClick = voiceInput,
                        searchIconSize = 24.dp, horizontalPadding = 16.dp, verticalPadding = 12.dp,
                        spacedBy = 10.dp, textStyle = ShelfBodyStyle.copy(fontSize = 16.sp),
                    )
                    QuimiaIconButton(
                        icon = Lucide.Settings2, size = QuimiaIconButtonSize.Medium,
                        containerColor = if (filtersVisible) tokens.secondaryPressed else tokens.surfaceBase,
                        iconColor = tokens.foregroundPrimary, contentDescription = "Mostrar ou ocultar filtros",
                        onClick = { filtersVisible = !filtersVisible },
                    )
                    if (tab == ShelfTab.Products) QuimiaIconButton(
                        icon = Lucide.Plus, size = QuimiaIconButtonSize.Medium,
                        containerColor = tokens.surfaceBase, iconColor = tokens.foregroundPrimary,
                        contentDescription = "Adicionar produto", onClick = onAddProduct,
                    )
                }
            }
            item(key = "shelf-filters") { QuimiaExpand(filtersVisible) {
                Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShelfSort.entries.take(3).forEach { item ->
                            ShelfFilterChip(item, sort == item, Modifier.weight(if (item == ShelfSort.Alphabetical) 1.37f else 1f)) {
                                if (apiProducts != null && tab == ShelfTab.Substances && item != ShelfSort.Alphabetical) onUnavailable("Esta ordenação ainda não está disponível.")
                                else { sort = if (sort == item) null else item; onSortChange(sort) }
                            }
                        }
                    }
                    ShelfFilterChip(ShelfSort.Oldest, sort == ShelfSort.Oldest, Modifier.width(97.dp)) {
                        if (apiProducts != null && tab == ShelfTab.Substances) onUnavailable("Esta ordenação ainda não está disponível para substâncias.")
                        else { sort = if (sort == ShelfSort.Oldest) null else ShelfSort.Oldest; onSortChange(sort) }
                    }
                }
            }
            }
            item { statusContent?.invoke() }
            if (showEmpty && ((tab == ShelfTab.Products && products.isEmpty()) || (tab == ShelfTab.Substances && substances.isEmpty()))) item {
                QuimiaEmptyState(
                    modifier = Modifier.padding(top = 24.dp), fillAvailableSpace = false,
                    title = if (tab == ShelfTab.Substances) "Nenhuma substância informada" else "Nenhum produto encontrado",
                    description = if (tab == ShelfTab.Substances) "As substâncias dependem da composição cadastrada dos produtos. Experimente outra pesquisa ou confira as informações disponíveis."
                        else if (savedIds.isEmpty()) "Guarde produtos no Catálogo para montar sua estante." else "Experimente outra pesquisa ou filtro.",
                    titleTextStyle = Typography.titleMedium, descriptionTextStyle = Typography.bodySmall,
                )
            } else if (tab == ShelfTab.Substances) items(substances, key = { it.id }) { substance ->
                ShelfSubstanceCard(
                    substance = substance,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = if (substance.id == substances.first().id) 24.dp else 16.dp),
                    expanded = substance.id in expandedSubstanceIds,
                    favorite = substance.id in favoriteSubstanceIds,
                    onExpand = { expandedSubstanceIds = expandedSubstanceIds.toggle(substance.id) },
                    onFavorite = { if (apiSubstances != null) onUnavailable("Favoritos indisponíveis no momento.") else favoriteSubstanceIds = favoriteSubstanceIds.toggle(substance.id) },
                    onRoom = { if (apiSubstances != null) onUnavailable("Consulte o cômodo nos produtos que contêm esta substância.") else roomId = substance.id },
                    onMix = { if (onSubstanceMix != null) onSubstanceMix(substance.id) else onMixProduct(substance.id) },
                    onMixtures = { if (onSubstanceMixtures != null) onSubstanceMixtures(substance.id) else onViewMixtures(substance.id) },
                )
            } else items(products, key = { it.id }) { product ->
                ShelfProductCard(
                    product, expanded = product.id in expandedIds, favorite = product.id in favoriteIds,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = if (product.id == products.first().id) 24.dp else 16.dp),
                    onExpand = {
                        if (product.id !in expandedIds) onApiExpand(product.id)
                        expandedIds = if (product.id in expandedIds) expandedIds - product.id else expandedIds + product.id
                    },
                    onFavorite = { if (apiProducts != null) onApiFavorite?.invoke(product.id, product.id !in favoriteIds)
                        else onFavoriteIdsChange(if (product.id in favoriteIds) favoriteIds - product.id else (favoriteIds + product.id).distinct()) },
                    onActions = { if (onApiActions != null) onApiActions(product) else actionId = product.id }, onRoom = {
                        if (onApiRoom != null) onApiRoom(product.id) else {
                        val linkedRooms = roomsForProduct(rooms, product.id)
                        if (linkedRooms.size == 1) onOpenRoom(linkedRooms.single().id)
                        else roomId = product.id
                        }
                    },
                    onMix = { onMixProduct(product.id) },
                    protectionContent = protectionContent?.let { { it(product.id) } },
                )
            }
            item { footerContent?.invoke() }
        }
        if (!overlayVisible) {
            QuimiaMenuBarBackdrop(Modifier.align(Alignment.BottomCenter))
            QuimiaMenuBar(
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 600.dp).fillMaxWidth()
                    .navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = QuimiaMenuBarBottomSpacing),
                selectedIndex = 2, onItemSelected = onMenuItemSelected, fabSpacing = 25.dp,
                fab = { QuimiaFAB(onClick = { onAiClick?.invoke() }, contentDescription = "Abrir assistente de IA") },
            )
        }
    }

    sampleCatalogProducts.find { it.id == actionId }?.let { product ->
        ShelfActionsPanel(
            product, onDismiss = { actionId = null },
            onRemove = { actionId = null; removeId = product.id },
            onMixtures = { actionId = null; onViewMixtures(product.id) },
            onInformation = { actionId = null; detailId = product.id },
        )
    }
    sampleCatalogProducts.find { it.id == detailId }?.let { product ->
        ShelfProductInformation(product, onDismiss = { detailId = null })
    }
    sampleCatalogProducts.find { it.id == removeId }?.let { product ->
        ShelfRemoveConfirmation(
            product, onDismiss = { removeId = null },
            onConfirm = {
                onSavedIdsChange(savedIds - product.id)
                onFavoriteIdsChange(favoriteIds - product.id)
                expandedIds = expandedIds - product.id
                removeId = null
                successVisible = true
            },
        )
    }
    roomId?.let { productId ->
        val linkedRooms = if (tab == ShelfTab.Products) roomsForProduct(rooms, productId) else emptyList()
        if (linkedRooms.isEmpty()) {
            ShelfNoRoomConfirmation(onDismiss = { roomId = null }, onCreate = {
                roomId = null
                onCreateRoom(productId)
            })
        } else {
            ShelfRoomsPanel(rooms = linkedRooms, onDismiss = { roomId = null }, onOpenRoom = {
                roomId = null
                onOpenRoom(it)
            })
        }
    }
}

@Composable
private fun ShelfFilterChip(item: ShelfSort, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    QuimiaChip(modifier = modifier, label = item.label, selected = selected, centerContent = true, onClick = onClick)
}

@Composable
private fun ShelfProductCard(
    product: CatalogProduct, expanded: Boolean, favorite: Boolean, modifier: Modifier,
    onExpand: () -> Unit, onFavorite: () -> Unit, onActions: () -> Unit, onRoom: () -> Unit, onMix: () -> Unit,
    protectionContent: (@Composable () -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    QuimiaCard(
        title = product.name, showTitle = false, showDescription = false,
        primaryActionLabel = null, secondaryActionLabel = null,
        modifier = modifier, contentPadding = 24.dp, sectionSpacing = 0.dp, endIcon = null,
    ) {
        Row(Modifier.fillMaxWidth().quimiaClickable(onClick = onExpand), verticalAlignment = Alignment.CenterVertically) {
            if (product.remote) CatalogImage(product, modifier = Modifier.size(45.dp)) else QuimiaImage(
                painter = painterResource(if (product.id.startsWith("squeeze")) R.drawable.shelf_mr_musculo_figma else product.detailImageRes), contentDescription = product.name,
                modifier = Modifier.size(45.dp),
                backgroundColor = tokens.surfaceBase,
                contentScale = if (product.id.startsWith("squeeze")) ContentScale.Crop else ContentScale.Fit,
            )
            Column(Modifier.weight(1f).padding(start = 10.dp, end = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    if (product.id.startsWith("squeeze")) "Limpador cozinha\nsqueeze" else product.name,
                    style = ShelfBodyStyle, color = tokens.foregroundPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when { product.remote -> product.brandName ?: "Marca não informada"; product.id.startsWith("squeeze") -> "Mr Músculos"; product.id == "veja" -> "Veja"; else -> "Omo" },
                    style = ShelfBodyStyle, color = tokens.foregroundSubtle,
                )
            }
            QuimiaIconButton(
                icon = Lucide.ChevronDown, iconRotationDegrees = if (expanded) 180f else 0f,
                size = QuimiaIconButtonSize.Medium, containerColor = tokens.surfaceBase,
                iconColor = tokens.foregroundPrimary, contentDescription = if (expanded) "Recolher produto" else "Expandir produto",
                onClick = onExpand,
            )
        }
        QuimiaExpand(expanded) {
            Spacer(Modifier.height(32.dp))
            if (protectionContent != null) protectionContent()
            else QuimiaListItem(title = "Como se proteger?", subtitle = "Description")
        }
        Spacer(Modifier.height(32.dp))
        ShelfCardActions(favorite, false, onFavorite, onActions, onRoom, onMix)
    }
}

@Composable
private fun ShelfSubstanceCard(
    substance: ShelfSubstance, expanded: Boolean, favorite: Boolean,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit, onFavorite: () -> Unit, onRoom: () -> Unit,
    onMix: () -> Unit, onMixtures: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    QuimiaCard(
        title = substance.name, showTitle = false, showDescription = false,
        primaryActionLabel = null, secondaryActionLabel = null, sectionSpacing = 0.dp, endIcon = null,
        modifier = modifier, contentPadding = 24.dp,
    ) {
        Row(Modifier.fillMaxWidth().quimiaClickable(onClick = onExpand), verticalAlignment = Alignment.CenterVertically) {
            Text(substance.name, Modifier.weight(1f), style = ShelfBodyStyle, color = tokens.textPrimary)
            QuimiaIconButton(
                icon = Lucide.ChevronDown, iconRotationDegrees = if (expanded) 180f else 0f,
                containerColor = tokens.surfaceBase, size = QuimiaIconButtonSize.Medium,
                contentDescription = if (expanded) "Recolher substância" else "Expandir substância",
                onClick = onExpand,
            )
        }
        QuimiaExpand(expanded) {
            Spacer(Modifier.height(32.dp))
            CatalogDescriptionCard(substance.description)
            substance.cas?.takeIf { it.isNotBlank() }?.let { Text("CAS: $it", style = ShelfBodyStyle, color = tokens.textSecondary) }
            if (substance.classes.isNotEmpty()) Text(substance.classes.joinToString(", "), style = ShelfBodyStyle, color = tokens.textSecondary)
            if (substance.products.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Presente em: ${substance.products.joinToString(", ")}", style = ShelfBodyStyle, color = tokens.textPrimary)
            }
        }
        Spacer(Modifier.height(32.dp))
        ShelfCardActions(favorite, true, onFavorite, onMixtures, onRoom, onMix)
    }
}

@Composable
private fun ShelfCardActions(
    favorite: Boolean, substance: Boolean, onFavorite: () -> Unit,
    onActions: () -> Unit, onRoom: () -> Unit, onMix: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 264.dp
        val icons: @Composable () -> Unit = {
            QuimiaIconButton(
                modifier = Modifier.quimiaSelectionFeedback(favorite).then(if (favorite) Modifier.border(2.dp, ShelfFavoriteColor, CircleShape) else Modifier),
                icon = Lucide.Heart,
                iconPainter = if (favorite) painterResource(R.drawable.figma_heart_filled) else null,
                size = QuimiaIconButtonSize.Medium,
                containerColor = if (favorite) tokens.surfaceBase else tokens.secondary,
                iconColor = if (favorite) Color.Unspecified else tokens.foregroundPrimary,
                contentDescription = if (favorite) "Remover dos favoritos" else "Favoritar",
                onClick = onFavorite,
            )
            QuimiaIconButton(icon = if (substance) Lucide.Atom else Lucide.Menu, size = QuimiaIconButtonSize.Medium,
                containerColor = tokens.secondary, contentDescription = if (substance) "Ver misturas" else "Abrir ações do produto", onClick = onActions)
            QuimiaIconButton(icon = Lucide.Bath, size = QuimiaIconButtonSize.Medium,
                containerColor = tokens.secondary, contentDescription = "Ver cômodo", onClick = onRoom)
        }
        val mix: @Composable (Modifier) -> Unit = { modifier ->
            QuimiaButton(modifier = modifier.height(43.dp), text = "Misturar", textStyle = ShelfBodyStyle,
                iconSize = 0, espacamento = 0, onClick = onMix)
        }
        if (compact) Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { icons() }
            mix(Modifier.fillMaxWidth())
        } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icons()
            mix(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ShelfSuccessBanner(onDismiss: () -> Unit) {
    val tokens = quimiaColorTokens()
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()
            .background(tokens.surfaceBase, CircleShape).padding(12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuimiaIconButton(icon = Lucide.Check, size = QuimiaIconButtonSize.Medium, containerColor = tokens.success, iconColor = tokens.foregroundPrimary)
        Text("Produto removido com sucesso", Modifier.weight(1f), style = ShelfBodyStyle, color = tokens.foregroundPrimary)
        QuimiaIconButton(
            icon = Lucide.X, size = QuimiaIconButtonSize.Small, containerColor = tokens.surfaceBase,
            iconColor = tokens.foregroundPrimary, contentDescription = "Fechar confirmação", onClick = onDismiss,
        )
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "GitHub · Estante preenchida · 360 × 900", group = "GitHub Screens", showBackground = true, widthDp = 360, heightDp = 900, showSystemUi = true)
@Preview(name = "Figma · Estante referência", group = "Figma", showBackground = true, widthDp = 360, heightDp = 1044, showSystemUi = true)
@Preview(name = "Estante compacta", showBackground = true, widthDp = 360, heightDp = 800, showSystemUi = true)
@Preview(name = "Estante telefone compacto", group = "Phone QA", widthDp = 320, heightDp = 640, showSystemUi = true)
@Preview(name = "Estante tablet retrato", group = "Tablet QA", widthDp = 800, heightDp = 1340, showSystemUi = true)
@Preview(name = "Estante tablet paisagem", group = "Tablet QA", widthDp = 1340, heightDp = 800, showSystemUi = true)
@Composable
private fun ShelfScreenPreview() {
    QuimiaTheme(darkTheme = false) {
        var ids by remember { mutableStateOf(listOf("squeeze", "squeeze-2")) }
        var favorites by remember { mutableStateOf(listOf("squeeze")) }
        ShelfScreen(ids, { ids = it }, favoriteIds = favorites, onFavoriteIdsChange = { favorites = it })
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Estante com filtros", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun ShelfFiltersPreview() {
    QuimiaTheme(false) {
        var ids by remember { mutableStateOf(sampleCatalogProducts.map { it.id }) }
        ShelfScreen(ids, { ids = it }, initialFiltersVisible = true)
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Substâncias", group = "Figma", widthDp = 360, heightDp = 1074, showSystemUi = true)
@Composable
private fun ShelfSubstancesPreview() {
    QuimiaTheme(false) { ShelfScreen(emptyList(), {}, initialSubstancesTab = true, initialFavoriteSubstanceIds = sampleShelfSubstances.map { it.id }) }
}

@Preview(name = "Estante vazia · fonte ampliada", group = "Acessibilidade", widthDp = 320, heightDp = 720, fontScale = 1.5f, showSystemUi = true)
@Composable
private fun ShelfEmptyPreview() {
    QuimiaTheme(false) { ShelfScreen(emptyList(), {}) }
}
