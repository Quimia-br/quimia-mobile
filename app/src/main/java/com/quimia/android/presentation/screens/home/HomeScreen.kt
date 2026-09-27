package com.quimia.android.presentation.screens.home

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import card.QuimiaCard
import card.QuimiaCardLayout
import com.composables.icons.lucide.Atom
import com.composables.icons.lucide.GalleryVerticalEnd
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.quimia.android.utils.AnalyticsLogger
import header.QuimiaHeader
import menuBar.QuimiaMenuBar
import buttons.QuimiaFAB
import shortCut.QuimiaShortCut
import shortCut.QuimiaShortCutColor
import shortCut.QuimiaShortCutType
import states.QuimiaEmptyState
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import title.QuimiaSectionTitle
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    avatarInitials: String = "Q",
    avatarPhoto: Painter? = null,
    onSearch: ((String) -> Unit)? = null,
    onMicClick: (() -> Unit)? = null,
    onBellClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onFindDropOffClick: (() -> Unit)? = null,
    onMixturesClick: (() -> Unit)? = null,
    onShelfClick: (() -> Unit)? = null,
    onMixClick: (() -> Unit)? = null,
    onMenuItemSelected: ((Int) -> Unit)? = null,
    onAiClick: (() -> Unit)? = null,
) {
    HomeSystemBarsEffect()

    val tokens = quimiaColorTokens()
    var searchValue by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun unavailable(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature ainda não está disponível") }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.surfaceBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(276.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tokens.brandSecondary.copy(alpha = 0.86f),
                            tokens.surfaceBackground.copy(alpha = 0.72f),
                            tokens.surfaceBackground,
                        )
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val topSpacing = (72.dp - WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()).coerceAtLeast(16.dp)

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = 89.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(topSpacing))

                QuimiaHeader(
                    value = searchValue,
                    onValueChange = { searchValue = it },
                    placeholder = "Pesquisar",
                    searchBarPadding = 12.dp,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    searchTextStyle = Typography.bodyMedium,
                    actionContainerColor = tokens.surfaceBase,
                    onSearch = {
                        AnalyticsLogger.logInteraction("home", "search_submitted")
                        if (onSearch != null) onSearch(searchValue) else unavailable("Busca")
                    },
                    onMicClick = {
                        AnalyticsLogger.logInteraction("home", "microphone_tapped")
                        if (onMicClick != null) onMicClick() else unavailable("Busca por voz")
                    },
                    onBellClick = {
                        AnalyticsLogger.logInteraction("home", "notifications_tapped")
                        if (onBellClick != null) onBellClick() else unavailable("Notificações")
                    },
                    onAvatarClick = {
                        AnalyticsLogger.logInteraction("home", "profile_tapped")
                        if (onAvatarClick != null) onAvatarClick() else unavailable("Perfil")
                    },
                    avatarInitials = avatarInitials,
                    avatarPhoto = avatarPhoto,
                )

                Spacer(modifier = Modifier.height(36.dp))

                HomeQuickActions(
                    onFindDropOffClick = {
                        AnalyticsLogger.logInteraction("home", "find_drop_off_tapped")
                        if (onFindDropOffClick != null) onFindDropOffClick() else unavailable("Pontos de descarte")
                    },
                    onMixturesClick = {
                        AnalyticsLogger.logInteraction("home", "mixtures_tapped")
                        if (onMixturesClick != null) onMixturesClick() else unavailable("Misturas")
                    },
                    onShelfClick = {
                        AnalyticsLogger.logInteraction("home", "shelf_tapped")
                        if (onShelfClick != null) onShelfClick() else unavailable("Minha estante")
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))

                QuimiaSectionTitle(
                    title = "Histórico de misturas",
                    titleSize = 20.sp,
                    horizontalPadding = 32.dp,
                    verticalPadding = 12.dp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                QuimiaEmptyState(
                    modifier = Modifier.fillMaxWidth(),
                    fillAvailableSpace = false,
                    icon = Lucide.Atom,
                    iconSize = 76.dp,
                    contentSpacing = 24.dp,
                    buttonSpacing = 24.dp,
                    buttonTextStyle = Typography.bodyMedium,
                    buttonModifier = Modifier.width(158.dp),
                    title = "Você ainda não fez\nnenhuma mistura",
                    titleTextStyle = Typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    ),
                    buttonLabel = "Misturar",
                    onButtonClick = {
                        AnalyticsLogger.logInteraction("home", "start_mixing_tapped")
                        if (onMixClick != null) onMixClick() else unavailable("Misturar")
                    },
                )

            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                QuimiaMenuBar(
                    modifier = Modifier.fillMaxWidth(),
                    selectedIndex = 0,
                    onItemSelected = { item ->
                        AnalyticsLogger.logInteraction("home", "menu_item_$item")
                        if (onMenuItemSelected != null) {
                            onMenuItemSelected(item)
                        } else if (item != 0) {
                            unavailable(listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja").getOrElse(item) { "Esta tela" })
                        }
                    },
                    fab = { QuimiaFAB(onClick = {
                        AnalyticsLogger.logInteraction("home", "fab_tapped")
                        if (onAiClick != null) onAiClick() else unavailable("Assistente de IA")
                    }, contentDescription = "Abrir assistente de IA") },
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 88.dp),
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun HomeSystemBarsEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
            ?: return@DisposableEffect onDispose {}
        val controller = WindowInsetsControllerCompat(window, view)
        val previousStatusBarColor = window.statusBarColor
        val previousLightStatusBars = controller.isAppearanceLightStatusBars

        window.statusBarColor = AndroidColor.TRANSPARENT
        controller.isAppearanceLightStatusBars = true

        onDispose {
            window.statusBarColor = previousStatusBarColor
            controller.isAppearanceLightStatusBars = previousLightStatusBars
        }
    }
}

@Composable
private fun HomeQuickActions(
    modifier: Modifier = Modifier,
    onFindDropOffClick: () -> Unit,
    onMixturesClick: () -> Unit,
    onShelfClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val compactProgress = ((maxWidth.value - 320f) / 40f).coerceIn(0f, 1f)
        val rowPadding = 24.dp
        val itemSpacing = (12f + 4f * compactProgress).dp
        val rowHeight = (224f + 24f * compactProgress).dp
        val leftWeight = 1.08f
        val rightWeight = 0.92f
        val shortcutPadding = (12f + 4f * compactProgress).dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowHeight)
                .height(rowHeight)
                .padding(horizontal = rowPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            QuimiaCard(
                modifier = Modifier
                    .weight(leftWeight)
                    .fillMaxHeight(),
                title = "Meu ponto de\ndescarte",
                description = "Não cadastrado",
                icon = Lucide.MapPin,
                primaryActionLabel = null,
                secondaryActionLabel = "Encontrar",
                layout = QuimiaCardLayout.Stacked,
                contentPadding = 16.dp,
                paddingValues = PaddingValues(
                    start = 16.dp,
                    top = 24.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                sectionSpacing = 18.dp,
                actionHeight = 43.dp,
                iconSize = 22.dp,
                titleTextStyle = Typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                ),
                descriptionTextStyle = Typography.bodySmall,
                actionTextStyle = Typography.bodyMedium,
                onSecondaryActionClick = onFindDropOffClick,
            )

            Column(
                modifier = Modifier.weight(rightWeight),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                QuimiaShortCut(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    type = QuimiaShortCutType.Large,
                    color = QuimiaShortCutColor.White,
                    icon = Lucide.Atom,
                    text = "Misturas",
                    showTag = false,
                    useDefaultSize = false,
                    contentPadding = shortcutPadding,
                    textStyle = Typography.bodyMedium.copy(lineHeight = 18.sp),
                    onClick = onMixturesClick,
                )
                QuimiaShortCut(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    type = QuimiaShortCutType.Large,
                    color = QuimiaShortCutColor.White,
                    icon = Lucide.GalleryVerticalEnd,
                    text = "Minha estante",
                    showTag = false,
                    useDefaultSize = false,
                    contentPadding = shortcutPadding,
                    textStyle = Typography.bodyMedium.copy(lineHeight = 18.sp),
                    onClick = onShelfClick,
                )
            }
        }
    }
}

@Preview(name = "Reference 360", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 900)
@Preview(name = "Android 420", showBackground = true, showSystemUi = true, widthDp = 420, heightDp = 911)
@Preview(name = "Compact 320", showBackground = true, showSystemUi = true, widthDp = 320, heightDp = 640)

@Preview(name = "Tab A7 Lite portrait - assumed 240 dpi", group = "Tablet QA", showBackground = true,
    device = "spec:width=800px,height=1340px,dpi=240", showSystemUi = true)
@Preview(name = "Tab A7 Lite landscape - assumed 240 dpi", group = "Tablet QA", showBackground = true,
    device = "spec:width=1340px,height=800px,dpi=240", showSystemUi = true)
@Preview(name = "Tablet 800 dp", group = "Tablet QA", showBackground = true,
    widthDp = 800, heightDp = 1340, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    QuimiaTheme(darkTheme = false) {
        HomeScreen()
    }
}
