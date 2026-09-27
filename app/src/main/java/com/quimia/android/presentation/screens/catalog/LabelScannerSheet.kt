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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.io.File
import kotlinx.coroutines.launch
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LabelScannerSheet(visible: Boolean, onDismiss: () -> Unit) {
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
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetMaxWidth = 420.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        dragHandle = null,
    ) {
        LabelScannerContent(
            imageUri = imageUri,
            error = error,
            onClose = { scope.launch { sheetState.hide(); onDismiss() } },
            onChooseImage = {
                error = null
                runCatching {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }.onFailure { error = "Não foi possível abrir a galeria. Tente novamente." }
            },
            onConfigureCamera = {
                // The system camera owns camera permission; no broad media permission is needed.
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
        )
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
) {
    val tokens = quimiaColorTokens()
    val hasImage = imageUri != null || previewImage != null
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 24.dp),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).width(64.dp).height(2.dp)
            .background(tokens.secondary, CircleShape))
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Escanear rótulo", style = Typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Normal), color = tokens.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Aponte a câmera para\no rótulo do seu produto\npara achá-lo",
                    style = Typography.bodySmall.copy(lineHeight = 15.sp), color = tokens.textSecondary)
            }
            QuimiaIconButton(
                icon = Lucide.X, onClick = onClose, contentDescription = "Fechar scanner",
                size = QuimiaIconButtonSize.Small, containerColor = tokens.secondary,
                iconColor = tokens.textPrimary,
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().aspectRatio(0.86f).clip(RoundedCornerShape(24.dp))
                .background(tokens.secondary), contentAlignment = Alignment.Center,
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
                    Box(Modifier.size(64.dp).background(tokens.surfaceBase, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Lucide.CameraOff, null, Modifier.size(22.dp), tint = tokens.textPrimary)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Câmera desabilitada", style = Typography.bodyMedium, color = tokens.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Abra a câmera para\nfotografar o rótulo", style = Typography.bodySmall,
                        color = tokens.textSecondary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    QuimiaButton(
                        modifier = Modifier.width(144.dp).heightIn(min = 40.dp),
                        text = "Configurar", textStyle = Typography.bodySmall,
                        iconSize = 0, espacamento = 0, containerColor = tokens.surfaceBase,
                        onClick = onConfigureCamera,
                    )
                }
            }
        }
        error?.let {
            Text(it, Modifier.padding(top = 12.dp), color = tokens.textPrimary, style = Typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        QuimiaButton(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            text = if (hasImage) "Escolher outra imagem" else "Escolher da galeria",
            iconLeftVector = if (hasImage) null else Lucide.Upload,
            iconSize = 18, espacamento = if (hasImage) 0 else 16,
            textStyle = Typography.bodyMedium, containerColor = tokens.secondary,
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
