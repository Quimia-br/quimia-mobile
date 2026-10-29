package com.quimia.android.presentation.screens.catalog

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import coil.compose.AsyncImage
import com.composables.icons.lucide.CameraOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Upload
import com.composables.icons.lucide.X
import com.quimia.android.R
import com.quimia.android.presentation.system.HideSystemNavigationBar
import java.io.File
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import theme.quimiaContrastBorder
import theme.quimiaContrastOutline

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LabelScannerSheet(visible: Boolean, onDismiss: () -> Unit, onBarcode: ((String) -> Unit)? = null) {
    if (onBarcode != null) {
        if (visible) CatalogBarcodeScannerSheet(onDismiss, onBarcode)
        return
    }
    val context = LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            imageUri = uri.toString()
            error = null
        }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) {
            imageUri = pendingCameraUri
            error = null
        }
        pendingCameraUri = null
    }
    if (!visible) return

    val tokens = quimiaColorTokens()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp),
        sheetMaxWidth = 560.dp,
        shape = RoundedCornerShape(36.dp),
        containerColor = tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        scrimColor = CatalogModalScrimColor,
        dragHandle = null,
    ) {
        Surface(shape = RoundedCornerShape(36.dp), color = tokens.surfaceBase, border = quimiaContrastBorder()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.padding(top = 8.dp).width(64.dp).height(2.dp)
                        .background(tokens.secondary, CircleShape),
                )
                LabelScannerContent(
                    imageUri = imageUri,
                    error = error,
                    onClose = onDismiss,
                    onChooseImage = {
                        error = null
                        imageUri = null
                        runCatching {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }.onFailure { error = "Não foi possível abrir a galeria. Tente novamente." }
                    },
                    onConfigureCamera = {
                        imageUri = null
                        runCatching {
                            val directory = File(context.cacheDir, "label-images").apply { mkdirs() }
                            val photo = File.createTempFile("label-", ".jpg", directory)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.label-images", photo)
                            pendingCameraUri = uri.toString()
                            camera.launch(uri)
                        }.onFailure {
                            pendingCameraUri = null
                            error = "Câmera indisponível. Você pode escolher uma imagem da galeria."
                        }
                    },
                    onImageError = { error = "Não foi possível ler esta imagem. Escolha outra foto." },
                    showDragHandle = false,
                )
            }
        }
    }
}

@Composable
internal fun LabelScannerContent(
    imageUri: String?,
    onClose: () -> Unit,
    onChooseImage: () -> Unit,
    onConfigureCamera: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    onImageError: () -> Unit = {},
    previewImage: Painter? = null,
    showDragHandle: Boolean = true,
    barcodeMode: Boolean = false,
) {
    HideSystemNavigationBar()
    val tokens = quimiaColorTokens()
    val hasImage = imageUri != null || previewImage != null
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp).padding(top = 32.dp, bottom = 32.dp).fillMaxHeight(),
    ) {
        if (showDragHandle) {
            Box(
                Modifier.align(Alignment.CenterHorizontally).width(262.dp).height(27.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.width(64.dp).height(2.dp).background(tokens.secondary, CircleShape))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(if (barcodeMode) "Escanear código de barras" else "Escanear rótulo", style = Typography.titleLarge, color = tokens.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(if (barcodeMode) "Fotografe o código de barras do produto para buscá-lo no catálogo." else "Aponte a câmera para\no rótulo do seu produto\npara achá-lo",
                    style = Typography.bodyMedium, color = tokens.textSecondary)
            }
            QuimiaIconButton(
                icon = Lucide.X, onClick = onClose, contentDescription = "Fechar scanner",
                size = QuimiaIconButtonSize.Small, containerColor = tokens.secondary,
                iconColor = tokens.textPrimary,
            )
        }
        Spacer(Modifier.height(28.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val photoHeight = (maxWidth / 0.86f).coerceAtMost(336.dp)
            Box(
                Modifier.fillMaxWidth().height(photoHeight).clip(RoundedCornerShape(24.dp))
                    .background(tokens.secondary).quimiaContrastOutline(RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center,
            ) {
                if (previewImage != null) {
                    Image(previewImage, "Imagem de exemplo do preview", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (imageUri != null) {
                    AsyncImage(
                        model = Uri.parse(imageUri), contentDescription = "Foto selecionada do rótulo",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                        onError = { onImageError() },
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(68.dp).background(tokens.surfaceBase, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Lucide.CameraOff, null, Modifier.size(24.dp), tint = tokens.textPrimary)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Câmera desabilitada", style = Typography.labelLarge, color = tokens.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Abra a câmera para\nfotografar o rótulo", style = Typography.bodyMedium,
                            color = tokens.textSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        QuimiaButton(
                            modifier = Modifier.width(152.dp).heightIn(min = 44.dp),
                            text = "Configurar", textStyle = Typography.bodyMedium,
                            iconSize = 0, espacamento = 0, containerColor = tokens.surfaceBase,
                            onClick = onConfigureCamera,
                        )
                    }
                }
            }
        }
        error?.let {
            Text(it, Modifier.padding(top = 12.dp), color = tokens.textPrimary, style = Typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        QuimiaButton(
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            text = if (hasImage) "Escolher outra imagem" else "Escolher da galeria",
            iconLeftVector = if (hasImage) null else Lucide.Upload,
            iconSize = 18, espacamento = if (hasImage) 0 else 16,
            textStyle = Typography.labelLarge, containerColor = tokens.secondary,
            onClick = onChooseImage,
        )
    }
}

@Preview(name = "Scanner · escolher", widthDp = 280, showBackground = true, backgroundColor = 0xFFE6E6E6)
@Preview(name = "Scanner · escolher · fonte ampliada", widthDp = 320, fontScale = 1.5f, showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun LabelScannerChoosePreview() {
    QuimiaTheme(darkTheme = false) {
        Surface(color = Color.White, shape = RoundedCornerShape(32.dp)) {
            LabelScannerContent(null, {}, {}, {})
        }
    }
}

@Preview(name = "Scanner · trocar (imagem ilustrativa)", widthDp = 280, showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun LabelScannerReplacePreview() {
    QuimiaTheme(darkTheme = false) {
        Surface(color = Color.White, shape = RoundedCornerShape(32.dp)) {
            LabelScannerContent(null, {}, {}, {}, previewImage = painterResource(R.drawable.ic_launcher_background))
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Preview(name = "Figma · Câmera sobre o catálogo", group = "Figma", widthDp = 360, heightDp = 805, showSystemUi = true)
@Composable
private fun LabelScannerOverlayPreview() {
    QuimiaTheme(darkTheme = false) {
        Box(Modifier.fillMaxSize()) {
            CatalogScreen(previewItemLimit = 3, initialBannerVisible = false)
            Box(Modifier.fillMaxSize().background(CatalogModalScrimColor))
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 560.dp)
                    .fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding(),
                color = quimiaColorTokens().surfaceBase,
                shape = RoundedCornerShape(36.dp),
                border = quimiaContrastBorder(),
            ) {
                LabelScannerContent(null, {}, {}, {})
            }
        }
    }
}
