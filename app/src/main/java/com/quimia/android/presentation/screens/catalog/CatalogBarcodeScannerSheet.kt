package com.quimia.android.presentation.screens.catalog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.quimia.android.presentation.system.HideSystemNavigationBar
import input.QuimiaInput
import kotlinx.coroutines.CancellationException
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogBarcodeScannerSheet(onDismiss: () -> Unit, onBarcode: (String) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val gate = remember { BarcodeCaptureGate() }
    val latestDismiss by rememberUpdatedState(onDismiss)
    val latestBarcode by rememberUpdatedState(onBarcode)
    var granted by remember { mutableStateOf(cameraPermission(context)) }
    var blocked by rememberSaveable { mutableStateOf(false) }
    var manual by rememberSaveable { mutableStateOf(false) }
    var manualCode by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var torch by rememberSaveable { mutableStateOf(false) }
    fun submit(raw: String) {
        gate.accept(raw)?.let { code ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            latestDismiss()
            latestBarcode(code)
        }
    }
    fun close() { gate.close(); latestDismiss() }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        granted = allowed
        blocked = !allowed && context.cameraActivity()?.let {
            !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } == true
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        picking = false
        if (uri != null) {
            imageUri = uri.toString()
        }
    }
    LaunchedEffect(Unit) { if (!granted && !manual) permission.launch(Manifest.permission.CAMERA) }
    DisposableEffect(gate) { onDispose { gate.close() } }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) granted = cameraPermission(context) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(imageUri) {
        val uri = imageUri ?: return@LaunchedEffect
        reading = true
        error = null
        try { submit(decodeCatalogBarcode(context, uri))
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) { error = "Não foi possível ler o código. Escolha uma imagem nítida do código de barras inteiro ou digite os números."
        } finally { reading = false; imageUri = null }
    }
    val tokens = quimiaColorTokens()
    ModalBottomSheet(onDismissRequest = ::close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp), sheetMaxWidth = 560.dp,
        containerColor = tokens.surfaceBase, contentColor = tokens.textPrimary, scrimColor = CatalogModalScrimColor,
        shape = RoundedCornerShape(36.dp), dragHandle = null) {
        HideSystemNavigationBar()
        Surface(shape = RoundedCornerShape(36.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Ler código de barras", style = Typography.titleLarge, color = tokens.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text("Enquadre o código da embalagem. A busca acontece assim que ele for lido.", style = Typography.bodyMedium, color = tokens.textSecondary)
                    }
                    QuimiaIconButton(icon = Lucide.X, size = QuimiaIconButtonSize.Small, contentDescription = "Fechar leitor de código de barras",
                        containerColor = tokens.secondary, onClick = ::close)
                }
                if (manual) {
                    QuimiaInput(value = manualCode, onValueChange = { manualCode = it.filter { char -> char in '0'..'9' }.take(64) },
                        label = "Código de barras", placeholder = "Digite os números da embalagem",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    QuimiaButton(text = "Buscar produto", enabled = catalogBarcodeNumber(manualCode) != null,
                        modifier = Modifier.fillMaxWidth().height(48.dp), iconSize = 0, espacamento = 0, onClick = { submit(manualCode) })
                } else if (granted && cameraError == null && imageUri == null && !picking && !reading) {
                    Box(Modifier.fillMaxWidth().height(286.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black)) {
                        BarcodeCameraPreview(Modifier.fillMaxSize(), torch, onCode = { code ->
                            if (!manual && !picking && imageUri == null && !reading && cameraError == null) submit(code)
                        },
                            onReady = { ready = true; hasFlash = it }, onError = { cameraError = it; ready = false })
                        BarcodeFrameGuide(Modifier.fillMaxSize())
                        Text(if (ready) "Mantenha o código inteiro no enquadramento" else "Abrindo câmera…",
                            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = .55f)).padding(12.dp),
                            color = Color.White, style = Typography.bodySmall, textAlign = TextAlign.Center)
                    }
                    if (hasFlash) QuimiaButton(text = if (torch) "Desligar lanterna" else "Ligar lanterna",
                        containerColor = tokens.secondary, textColor = tokens.textPrimary, modifier = Modifier.fillMaxWidth().height(44.dp),
                        iconSize = 0, espacamento = 0, onClick = { torch = !torch })
                } else if (reading || picking || imageUri != null) {
                    CatalogLoading(if (reading || imageUri != null) "Lendo código de barras…" else "Escolha uma foto do código de barras")
                } else {
                    BarcodeCameraNotice(cameraError ?: "Permita o acesso à câmera para ler o código de barras.",
                        action = if (cameraError != null) "Tentar câmera novamente" else if (blocked) "Abrir configurações" else "Permitir câmera",
                        onAction = {
                            if (cameraError != null) { cameraError = null; ready = false }
                            else if (blocked) runCatching {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                            }.onFailure { error = "Não foi possível abrir as configurações. Você pode digitar o código." }
                            else permission.launch(Manifest.permission.CAMERA)
                        })
                }
                error?.let { Text(it, style = Typography.bodyMedium, color = tokens.textPrimary) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuimiaButton(text = "Galeria", modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        containerColor = tokens.secondary, textColor = tokens.textPrimary, iconSize = 0, espacamento = 0, enabled = !reading,
                        onClick = {
                            error = null; imageUri = null; picking = true; ready = false; manual = false
                            runCatching { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                .onFailure { picking = false; error = "Não foi possível abrir a galeria. Você pode digitar o código." }
                        })
                    QuimiaButton(text = if (manual) "Usar câmera" else "Digitar código", modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        containerColor = tokens.secondary, textColor = tokens.textPrimary, iconSize = 0, espacamento = 0, enabled = !reading,
                        onClick = { manual = !manual; error = null; ready = false })
                }
                Text("Apenas o número do código de barras é usado para buscar o produto.", style = Typography.bodySmall, color = tokens.textSecondary)
            }
        }
    }
}

private fun cameraPermission(context: Context) = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
private fun Context.cameraActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) { if (current is Activity) return current; current = current.baseContext }
    return current as? Activity
}

@Composable
internal fun BarcodeFrameGuide(modifier: Modifier = Modifier) {
    val color = quimiaColorTokens().primary
    Canvas(modifier) {
        val left = size.width * .08f; val right = size.width * .92f
        val top = size.height * .24f; val bottom = size.height * .72f
        val shade = Color.Black.copy(alpha = .38f)
        drawRect(shade, size = androidx.compose.ui.geometry.Size(size.width, top))
        drawRect(shade, topLeft = Offset(0f, bottom), size = androidx.compose.ui.geometry.Size(size.width, size.height - bottom))
        drawRect(shade, topLeft = Offset(0f, top), size = androidx.compose.ui.geometry.Size(left, bottom - top))
        drawRect(shade, topLeft = Offset(right, top), size = androidx.compose.ui.geometry.Size(size.width - right, bottom - top))
        val length = 24.dp.toPx(); val stroke = 3.dp.toPx()
        listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom)).forEach { corner ->
            val dx = if (corner.x == left) length else -length
            val dy = if (corner.y == top) length else -length
            drawLine(color, corner, Offset(corner.x + dx, corner.y), strokeWidth = stroke)
            drawLine(color, corner, Offset(corner.x, corner.y + dy), strokeWidth = stroke)
        }
    }
}

@Composable
private fun BarcodeCameraNotice(message: String, action: String, onAction: () -> Unit) {
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().background(tokens.surfaceBackground, RoundedCornerShape(24.dp)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Lucide.Camera, null, modifier = Modifier.size(36.dp), tint = tokens.textPrimary)
        Text(message, style = Typography.bodyMedium, color = tokens.textSecondary, textAlign = TextAlign.Center)
        QuimiaButton(text = action, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), iconSize = 0, espacamento = 0, onClick = onAction)
    }
}

@Preview(name = "Leitor de código · enquadramento", widthDp = 360, heightDp = 286)
@Composable
private fun BarcodeGuidePreview() { QuimiaTheme(false) { Box(Modifier.fillMaxSize().background(Color(0xFF262626))) { BarcodeFrameGuide(Modifier.fillMaxSize()) } } }

@Preview(name = "Leitor de código · permissão", widthDp = 320, showBackground = true)
@Composable
private fun BarcodePermissionPreview() { QuimiaTheme(false) { BarcodeCameraNotice("Permita o acesso à câmera para ler o código de barras.", "Permitir câmera", {}) } }
