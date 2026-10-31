package com.quimia.android.presentation.components

import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RawRes
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

/** Silent local video; the fallback underneath stays visible until the first decoded frame. */
@Composable
internal fun ResourceVideo(
    @RawRes resource: Int,
    modifier: Modifier = Modifier,
    looping: Boolean = false,
    clipToCircle: Boolean = false,
    onFinished: () -> Unit = {},
    onFirstCycleFinished: () -> Unit = {},
    onFailure: () -> Unit = {},
) = key(resource) {
    val owner = LocalLifecycleOwner.current
    val finished by rememberUpdatedState(onFinished)
    val firstCycleFinished by rememberUpdatedState(onFirstCycleFinished)
    val failure by rememberUpdatedState(onFailure)
    val player = remember { MediaPlayer() }
    var prepared by remember { mutableStateOf(false) }
    var rendering by remember { mutableStateOf(false) }
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
            player.setOnPreparedListener(null); player.setOnCompletionListener(null)
            player.setOnErrorListener(null); player.setOnInfoListener(null); player.setOnVideoSizeChangedListener(null)
            player.release()
        }
    }
    LaunchedEffect(rendering) {
        if (!rendering) {
            delay(15_000)
            Log.w("QuimiaVideo", "First frame timed out for resource $resource")
            failure()
        }
    }
    // A zero-alpha layer can stop TextureView consuming buffers, preventing the first-frame event.
    AndroidView(modifier = modifier.graphicsLayer { alpha = if (rendering) 1f else .01f },
        factory = { context ->
            TextureView(context).apply {
                val textureView = this
                if (clipToCircle) {
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) { outline.setOval(0, 0, view.width, view.height) }
                    }
                    clipToOutline = true
                }
                var surface: Surface? = null
                var configured = false
                var videoWidth = 0
                var videoHeight = 0
                fun crop(viewWidth: Int, viewHeight: Int) {
                    if (videoWidth <= 0 || videoHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return
                    val videoAspect = videoWidth.toFloat() / videoHeight
                    val viewAspect = viewWidth.toFloat() / viewHeight
                    setTransform(Matrix().apply {
                        setScale(maxOf(1f, videoAspect / viewAspect), maxOf(1f, viewAspect / videoAspect),
                            viewWidth / 2f, viewHeight / 2f)
                    })
                }
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                        surface = Surface(texture)
                        try {
                            player.setSurface(surface)
                            if (!configured) {
                                configured = true
                                // The first cycle needs a real completion event, not a wall-clock timeout.
                                player.isLooping = false
                                player.setVolume(0f, 0f)
                                player.setDataSource(context, Uri.parse("android.resource://${context.packageName}/$resource"))
                                player.setOnVideoSizeChangedListener { _, w, h ->
                                    videoWidth = w; videoHeight = h; crop(textureView.width, textureView.height)
                                }
                                player.setOnPreparedListener {
                                    prepared = true
                                    if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) it.start()
                                }
                                player.setOnCompletionListener {
                                    firstCycleFinished()
                                    if (looping) {
                                        it.isLooping = true
                                        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) it.start()
                                    } else finished()
                                }
                                player.setOnErrorListener { _, what, extra ->
                                    Log.w("QuimiaVideo", "Playback failed for resource $resource: $what/$extra")
                                    failure(); true
                                }
                                player.setOnInfoListener { _, what, _ ->
                                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) rendering = true
                                    false
                                }
                                player.prepareAsync()
                            } else if (prepared && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) player.start()
                        } catch (error: Exception) { Log.w("QuimiaVideo", "Cannot prepare resource $resource", error); failure() }
                    }
                    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) { crop(width, height) }
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
