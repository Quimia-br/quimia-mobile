package com.quimia.android.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.quimia.android.R
import com.quimia.android.presentation.screens.kemi.KemiBackground
import kotlinx.coroutines.delay
import motion.LocalQuimiaMotionEnabled
import motion.QuimiaMotion
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

private data class DestinationLoad(val pending: Boolean, val navigationVisible: Boolean?, val page: Any?)
private val LocalDestinationLoading = staticCompositionLocalOf<(String, DestinationLoad) -> Unit> { { _, _ -> } }

/** Reports the initial request batch, never an in-page mutation, refresh or bottom sheet. */
@Composable
internal fun ReportDestinationLoading(destination: String, pending: Boolean, navigationVisible: Boolean? = null, page: Any? = null) {
    val report = LocalDestinationLoading.current
    SideEffect { report(destination, DestinationLoad(pending, navigationVisible, page)) }
}

@Composable
internal fun DestinationLoadingHost(
    destination: String,
    userId: String?,
    accountLoading: Boolean,
    navigationVisible: Boolean = false,
    content: @Composable (Boolean) -> Unit,
) {
    // A fresh entry cannot inherit a previous visit's "ready" report.
    val reports = remember(destination, userId) { mutableStateMapOf<String, DestinationLoad>() }
    val report: (String, DestinationLoad) -> Unit = remember(destination, userId) { { key, load -> reports[key] = load } }
    val dataScreen = destination in setOf("home", "catalog", "shelf", "rooms", "profile", "mixtures")
    val load = reports[destination]
    val entry = Triple(destination, userId, load?.page)
    val pending = rememberEntryLoading(load?.pending ?: dataScreen, entry)
    val menuShown = load?.navigationVisible ?: navigationVisible
    val motion = LocalQuimiaMotionEnabled.current
    var aiEntry by remember(destination, userId) { mutableStateOf(destination == "kemi" && motion) }
    var slowEntry by remember(entry) { mutableStateOf(false) }
    LaunchedEffect(entry, pending, menuShown) {
        if (pending && !menuShown) { delay(650); slowEntry = true } else slowEntry = false
    }
    // A short branded entrance to the assistant; it never waits for a chat response.
    LaunchedEffect(destination, motion) {
        if (aiEntry && motion) delay(450)
        aiEntry = false
    }
    val visible = brandedLoadingVisible(menuShown, accountLoading, aiEntry, pending, slowEntry)
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(visible) { if (visible) keyboard?.hide() }
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalDestinationLoading provides report) {
            Box(Modifier.fillMaxSize().then(if (visible) Modifier.clearAndSetSemantics { } else Modifier)) {
                content(visible)
            }
        }
        // Do not even keep an outgoing loading layer above a newly visible menu.
        if (!menuShown) {
            AnimatedVisibility(visible, enter = fadeIn(QuimiaMotion.tweenSpec(motion, 120)),
                exit = fadeOut(QuimiaMotion.tweenSpec(motion, QuimiaMotion.ScreenDissolve))) {
                BrandedLoadingScreen()
            }
        }
        BackHandler(enabled = accountLoading) { }
    }
}

/** Figma 2058:9127: the supplied walking-pig video repeats only while this loading layer exists. */
@Composable
internal fun BrandedLoadingScreen() {
    val tokens = quimiaColorTokens()
    BoxWithConstraints(Modifier.fillMaxSize().background(tokens.surfaceBackground)
        .pointerInput(Unit) { detectTapGestures { } }
        .semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "Carregando" }) {
        KemiBackground(Modifier.fillMaxWidth().height(280.dp))
        val size = minOf(138.dp, maxHeight * .32f)
        val animate = LocalQuimiaMotionEnabled.current && !LocalLaunchSplashVisible.current
        var failed by remember { mutableStateOf(false) }
        Box(Modifier.align(Alignment.Center).size(size, size * (137f / 138f)).clip(CircleShape)) {
            Image(painterResource(R.drawable.kemi_loading), null, Modifier.matchParentSize(), contentScale = ContentScale.Fit)
            if (animate && !failed) ResourceVideo(R.raw.quimia_loading, Modifier.matchParentSize(),
                looping = true, clipToCircle = true, onFailure = { failed = true })
        }
        Text("Carregando...", Modifier.align(Alignment.Center).offset(y = size / 2 + 45.dp),
            style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
    }
}
