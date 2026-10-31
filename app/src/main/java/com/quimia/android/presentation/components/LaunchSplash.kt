package com.quimia.android.presentation.components

import android.app.Activity
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quimia.android.R
import kotlinx.coroutines.delay
import motion.LocalQuimiaMotionEnabled
import motion.QuimiaMotion

/** The app and session prepare underneath the one-time launch animation. */
@Composable
internal fun LaunchSplash(content: @Composable () -> Unit) {
    var finished by rememberSaveable { mutableStateOf(false) }
    val motion = LocalQuimiaMotionEnabled.current
    LaunchedEffect(motion) { if (!motion) finished = true }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (!finished) Modifier.clearAndSetSemantics { } else Modifier)) { content() }
        AnimatedVisibility(!finished && motion, exit = fadeOut(QuimiaMotion.tweenSpec(motion, QuimiaMotion.ScreenDissolve))) {
            LaunchVideo { finished = true }
        }
        BackHandler(enabled = !finished) { }
    }
}

@Composable
private fun LaunchVideo(onFinished: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val finished by rememberUpdatedState(onFinished)
    val player = remember { MediaPlayer() }
    var prepared by remember { mutableStateOf(false) }
    var rendering by remember { mutableStateOf(false) }
    val window = (context as? Activity)?.window
    DisposableEffect(window) {
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        val wasLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (wasLight != null) controller?.isAppearanceLightStatusBars = wasLight }
    }
    DisposableEffect(player, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (prepared) runCatching {
                if (event == Lifecycle.Event.ON_PAUSE) player.pause()
                if (event == Lifecycle.Event.ON_RESUME) player.start()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            player.setOnPreparedListener(null); player.setOnCompletionListener(null); player.setOnErrorListener(null); player.setOnInfoListener(null)
            player.release()
        }
    }
    // A missing decoder or a damaged asset must never trap the user at launch.
    LaunchedEffect(Unit) { delay(6_000); finished() }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF2E2E2E))
        .pointerInput(Unit) { detectTapGestures { } }) {
        val width = minOf(maxWidth, maxHeight * (290f / 670f))
        AndroidView(modifier = Modifier.align(Alignment.Center).width(width).height(width * (670f / 290f))
            .graphicsLayer { alpha = if (rendering) 1f else 0f },
            factory = { viewContext ->
                TextureView(viewContext).apply {
                    var surface: Surface? = null
                    var configured = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                            surface = Surface(texture)
                            try {
                                player.setSurface(surface)
                                if (!configured) {
                                    configured = true
                                    player.setVolume(0f, 0f)
                                    player.setDataSource(viewContext, Uri.parse("android.resource://${viewContext.packageName}/${R.raw.quimia_splash}"))
                                    player.setOnPreparedListener {
                                        prepared = true
                                        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) it.start()
                                    }
                                    player.setOnCompletionListener { finished() }
                                    player.setOnErrorListener { _, _, _ -> finished(); true }
                                    player.setOnInfoListener { _, what, _ ->
                                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) rendering = true
                                        false
                                    }
                                    player.prepareAsync()
                                } else if (prepared && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) player.start()
                            } catch (_: Exception) { finished() }
                        }
                        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = Unit
                        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                            runCatching { if (prepared) player.pause(); player.setSurface(null) }
                            surface?.release(); surface = null
                            return true
                        }
                    }
                }
            })
    }
}
