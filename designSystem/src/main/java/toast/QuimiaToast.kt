package toast

import motion.quimiaEnter
import motion.LocalQuimiaMotionEnabled
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import feedback.LocalQuimiaSound
import feedback.QuimiaSound
import theme.quimiaSkeleton
import theme.QuimiaTextStyles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.X
import shapeIcon.QuimiaShapeIcon
import shapeIcon.QuimiaShapeIconSize
import shapeIcon.QuimiaShapeIconTheme
import theme.QuimiaTheme
import theme.RadiusFull
import theme.quimiaColorTokens
import theme.quimiaContrastBorder

enum class QuimiaToastType { Info, Success, Warning, Error, Loading }

@Composable
fun QuimiaToast(
    message: String,
    modifier: Modifier = Modifier,
    type: QuimiaToastType = QuimiaToastType.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    containerColor: Color? = null,
    leadingIcon: ImageVector? = null,
    description: String? = null,
    statusIconContainerColor: Color? = null,
    animateEntrance: Boolean = true,
) {
    val tokens = quimiaColorTokens()
    val motion = LocalQuimiaMotionEnabled.current
    val sound = LocalQuimiaSound.current
    var visible by remember(message) { mutableStateOf(true) }
    var closing by remember(message) { mutableStateOf(false) }
    val latestDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(closing, message) {
        if (closing) { if (motion) delay(180); latestDismiss?.invoke() }
    }
    val dismiss: () -> Unit = {
        if (!closing) {
            closing = true; visible = false
        }
    }
    LaunchedEffect(message, type) {
        when (type) {
            QuimiaToastType.Success -> sound(QuimiaSound.Success)
            QuimiaToastType.Error -> sound(QuimiaSound.Error)
            else -> Unit
        }
    }
    val accent = when (type) {
        QuimiaToastType.Info, QuimiaToastType.Loading -> tokens.foregroundPrimary
        QuimiaToastType.Success -> tokens.success
        QuimiaToastType.Warning -> tokens.warning
        QuimiaToastType.Error -> tokens.error
    }
    val iconTheme = when (type) {
        QuimiaToastType.Info, QuimiaToastType.Loading -> QuimiaShapeIconTheme.Secondary
        QuimiaToastType.Success -> QuimiaShapeIconTheme.Success
        QuimiaToastType.Warning -> QuimiaShapeIconTheme.Warning
        QuimiaToastType.Error -> QuimiaShapeIconTheme.Critical
    }
    val statusIcon = leadingIcon ?: when (type) {
        QuimiaToastType.Info, QuimiaToastType.Loading -> Lucide.Info
        QuimiaToastType.Success -> Lucide.Check
        QuimiaToastType.Warning -> Lucide.TriangleAlert
        QuimiaToastType.Error -> Lucide.X
    }

    AnimatedVisibility(visible = visible, modifier = modifier, enter = fadeIn(tween(if (motion) 180 else 0)),
        exit = fadeOut(tween(if (motion) 180 else 0)) + slideOutVertically(tween(if (motion) 180 else 0)) { -it / 4 }) {
    Surface(
        modifier = Modifier.widthIn(max = 600.dp).then(if (animateEntrance) Modifier.quimiaEnter() else Modifier).quimiaSkeleton(32.dp).fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(32.dp),
        color = containerColor ?: tokens.surfaceBase,
        contentColor = tokens.textPrimary,
        border = quimiaContrastBorder(),
    ) {
        BoxWithConstraints {
        val compact = maxWidth < 340.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 12.dp else 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (type == QuimiaToastType.Loading) Box(Modifier.size(if (compact) 40.dp else 50.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = tokens.foregroundPrimary, strokeWidth = 2.dp)
            } else QuimiaShapeIcon(
                size = if (compact) 40.dp else QuimiaShapeIconSize.Small.value,
                icon = statusIcon,
                theme = iconTheme,
                containerColor = statusIconContainerColor,
            )
            Column(
                modifier = Modifier.weight(1f).sizeIn(minHeight = 50.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = message,
                    color = tokens.foregroundPrimary,
                    style = QuimiaTextStyles.medium16,
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        color = tokens.foregroundSubtle,
                        style = QuimiaTextStyles.medium14,
                    )
                }
                if (actionLabel != null && onAction != null) TextButton(onClick = onAction) {
                    Text(text = actionLabel, color = accent)
                }
            }

            if (onDismiss != null) {
                IconButton(onClick = dismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Fechar aviso",
                        tint = tokens.foregroundSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        }
    }
    }
}

/** Keeps the previous notice mounted until its exit animation has finished. */
@Composable
fun QuimiaToastHost(message: String?, modifier: Modifier = Modifier, type: QuimiaToastType = QuimiaToastType.Info,
    description: String? = null, onDismiss: (() -> Unit)? = null) {
    var retained by remember { mutableStateOf(message) }
    var retainedType by remember { mutableStateOf(type) }
    var retainedDescription by remember { mutableStateOf(description) }
    val motion = LocalQuimiaMotionEnabled.current
    LaunchedEffect(message, type, description) {
        if (message != null) { retained = message; retainedType = type; retainedDescription = description }
    }
    AnimatedVisibility(visible = message != null, modifier = modifier,
        enter = fadeIn(tween(if (motion) 220 else 0)) + slideInVertically(tween(if (motion) 220 else 0)) { -it / 4 },
        exit = fadeOut(tween(if (motion) 180 else 0)) + slideOutVertically(tween(if (motion) 180 else 0)) { -it / 4 }) {
        retained?.let { QuimiaToast(it, type = retainedType, description = retainedDescription,
            onDismiss = onDismiss, animateEntrance = false) }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaToastPreview() {
    QuimiaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(50.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            listOf(QuimiaToastType.Info, QuimiaToastType.Error, QuimiaToastType.Success, QuimiaToastType.Warning).forEach { type ->
                QuimiaToast(message = "Title", description = "Description", type = type, onDismiss = {})
            }
        }
    }
}
