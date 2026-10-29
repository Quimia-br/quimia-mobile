package com.quimia.android.presentation.screens.catalog

import android.os.SystemClock
import android.util.Size
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Observer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun BarcodeCameraPreview(modifier: Modifier, torch: Boolean, onCode: (String) -> Unit,
    onReady: (Boolean) -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val latestCode by rememberUpdatedState(onCode)
    val latestReady by rememberUpdatedState(onReady)
    val latestError by rememberUpdatedState(onError)
    var camera by remember { mutableStateOf<Camera?>(null) }
    val view = remember(context) { PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleType = PreviewView.ScaleType.FILL_CENTER
        contentDescription = "Câmera para ler código de barras. Toque para ajustar o foco."
    } }
    DisposableEffect(view, owner) {
        val active = AtomicBoolean(true)
        val delivered = AtomicBoolean(false)
        val analyzerExecutor = Executors.newSingleThreadExecutor()
        val preview = Preview.Builder().build().apply { setSurfaceProvider(view.surfaceProvider) }
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)).build())
            .build()
        var lastFrame = 0L
        var badFrames = 0
        analysis.setAnalyzer(analyzerExecutor) { frame ->
            try {
                val now = SystemClock.elapsedRealtime()
                if (!active.get() || delivered.get() || now - lastFrame < 250) return@setAnalyzer
                lastFrame = now
                val crop = frame.cropRect
                val plane = frame.planes.first()
                val data = copyCatalogLuminance(plane.buffer.duplicate(), crop.width(), crop.height(), plane.rowStride, plane.pixelStride,
                    crop.left, crop.top)
                val code = decodeCatalogLuminance(crop.width(), crop.height(), data)
                badFrames = 0
                if (code != null && delivered.compareAndSet(false, true)) mainExecutor.execute {
                    if (active.get()) latestCode(code)
                }
            } catch (_: RuntimeException) {
                badFrames++
                if (badFrames == 10) mainExecutor.execute {
                    if (active.get()) latestError("Não foi possível analisar a imagem da câmera. Tente novamente ou use a galeria.")
                }
            } finally { frame.close() }
        }
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        val cameraObserver = Observer<CameraState> { current ->
            if (active.get() && current.error != null) latestError("A câmera está indisponível ou em uso. Tente novamente ou digite o código.")
        }
        val streamObserver = Observer<PreviewView.StreamState> { current ->
            if (active.get() && current == PreviewView.StreamState.STREAMING) latestReady(camera?.cameraInfo?.hasFlashUnit() == true)
        }
        future.addListener({
            if (active.get()) try {
                val current = future.get()
                provider = current
                camera = current.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                camera?.cameraInfo?.cameraState?.observe(owner, cameraObserver)
                view.previewStreamState.observe(owner, streamObserver)
            } catch (_: Exception) {
                latestError("Não foi possível abrir a câmera. Você pode tentar novamente, escolher uma foto ou digitar o código.")
            }
        }, mainExecutor)
        view.setOnTouchListener { child, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                child.performClick()
                val point = view.meteringPointFactory.createPoint(event.x, event.y)
                runCatching { camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build()) }
            }
            true
        }
        onDispose {
            active.set(false)
            analysis.clearAnalyzer()
            camera?.cameraInfo?.cameraState?.removeObserver(cameraObserver)
            view.previewStreamState.removeObserver(streamObserver)
            provider?.unbind(preview, analysis)
            camera = null
            view.setOnTouchListener(null)
            analyzerExecutor.shutdown()
        }
    }
    LaunchedEffect(camera, torch) { runCatching { camera?.cameraControl?.enableTorch(torch) } }
    AndroidView(factory = { view }, modifier = modifier)
}
