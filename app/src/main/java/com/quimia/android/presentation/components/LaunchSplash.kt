package com.quimia.android.presentation.components

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.view.WindowInsetsControllerCompat
import com.quimia.android.R
import kotlinx.coroutines.delay
import motion.LocalQuimiaMotionEnabled
import motion.QuimiaMotion

internal val LocalLaunchSplashVisible = staticCompositionLocalOf { false }

/** The app and session prepare underneath the one-time launch animation. */
@Composable
internal fun LaunchSplash(content: @Composable () -> Unit) {
    var finished by rememberSaveable { mutableStateOf(false) }
    val motion = LocalQuimiaMotionEnabled.current
    LaunchedEffect(motion) { if (!motion) finished = true }
    Box(Modifier.fillMaxSize()) {
        // Keep composing the routes immediately: session/profile/screen requests must not wait for video completion.
        CompositionLocalProvider(LocalLaunchSplashVisible provides !finished) {
            Box(Modifier.fillMaxSize().then(if (!finished) Modifier.clearAndSetSemantics { } else Modifier)) { content() }
        }
        AnimatedVisibility(!finished && motion, exit = fadeOut(QuimiaMotion.tweenSpec(motion, QuimiaMotion.ScreenDissolve))) {
            LaunchVideo { finished = true }
        }
        BackHandler(enabled = !finished) { }
    }
}

@Composable
private fun LaunchVideo(onFinished: () -> Unit) {
    val window = (LocalContext.current as? Activity)?.window
    val finished by rememberUpdatedState(onFinished)
    DisposableEffect(window) {
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        val wasLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (wasLight != null) controller.isAppearanceLightStatusBars = wasLight }
    }
    // A missing decoder, damaged asset or stalled playback must never trap the user at launch.
    LaunchedEffect(Unit) { delay(6_000); finished() }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF2E2E2E))
        .pointerInput(Unit) { detectTapGestures { } }) {
        val aspect = 784f / 1808f
        val width = minOf(maxWidth, maxHeight * aspect)
        ResourceVideo(R.raw.quimia_splash,
            modifier = Modifier.align(Alignment.Center).width(width).height(width / aspect),
            onFinished = { finished() }, onFailure = { finished() })
    }
}
