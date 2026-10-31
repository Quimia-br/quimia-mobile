package toast

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dismiss
import motion.QuimiaMotion
import motion.quimiaResize
import kotlin.math.abs
import motion.LocalQuimiaMotionEnabled
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import kotlinx.coroutines.delay
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
    compactLayout: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val motion = LocalQuimiaMotionEnabled.current
    val sound = LocalQuimiaSound.current
    val visibility = remember(message) { MutableTransitionState(!animateEntrance) }
    var closing by remember(message) { mutableStateOf(false) }
    var dragging by remember(message) { mutableStateOf(false) }
    var dragX by remember(message) { mutableFloatStateOf(0f) }
    var swipeDirection by remember(message) { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current.density
    val tracker = remember(message) { VelocityTracker() }
    val canDismiss = onDismiss != null && type != QuimiaToastType.Loading && !theme.LocalQuimiaSkeleton.current
    val targetX = if (closing && swipeDirection != 0f) swipeDirection * (widthPx + 32f * density) else dragX
    val offsetX by key(message) { animateFloatAsState(targetX, animationSpec = when {
        dragging -> snap()
        closing -> QuimiaMotion.tweenSpec(motion, 220)
        else -> QuimiaMotion.controlSpec(motion)
    }, label = "toastSwipe") }
    val latestDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(message) { visibility.targetState = true }
    LaunchedEffect(closing, message) {
        if (closing) { if (motion) delay(220); latestDismiss?.invoke() }
    }
    val requestDismiss: () -> Unit = {
        if (canDismiss && !closing) {
            closing = true; dragging = false; visibility.targetState = false
        }
    }
    val swipeModifier = if (!canDismiss) Modifier else Modifier.pointerInput(message, canDismiss) {
        detectHorizontalDragGestures(
            onDragStart = { if (!closing) { tracker.resetTracking(); dragX = offsetX; dragging = true } },
            onHorizontalDrag = { change, amount ->
                if (!closing) {
                    change.consume(); dragX += amount
                    tracker.addPosition(change.uptimeMillis, Offset(dragX, 0f))
                }
            },
            onDragEnd = {
                dragging = false
                if (!closing && shouldDismissToast(dragX, widthPx, tracker.calculateVelocity().x, density)) {
                    swipeDirection = if (dragX >= 0f) 1f else -1f
                    sound(QuimiaSound.Tap); requestDismiss()
                } else if (!closing) dragX = 0f
            },
            onDragCancel = { if (!closing) { dragging = false; dragX = 0f } },
        )
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

    AnimatedVisibility(visibleState = visibility, modifier = modifier,
        enter = fadeIn(QuimiaMotion.tweenSpec(motion, 180)) + slideInVertically(QuimiaMotion.tweenSpec(motion, 220)) { -it / 5 },
        exit = fadeOut(QuimiaMotion.tweenSpec(motion, 180)) + shrinkVertically(QuimiaMotion.tweenSpec(motion, 220), shrinkTowards = Alignment.Top)) {
    Surface(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().quimiaResize().onSizeChanged { widthPx = it.width.toFloat() }
            .then(swipeModifier).graphicsLayer {
                translationX = offsetX
                alpha = if (widthPx > 0f) (1f - abs(offsetX) / widthPx * .65f).coerceIn(.2f, 1f) else 1f
            }.quimiaSkeleton(32.dp).semantics {
                liveRegion = LiveRegionMode.Polite
                if (canDismiss) dismiss { requestDismiss(); true }
            },
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
                .padding(horizontal = if (compact) 12.dp else 16.dp, vertical = if (compactLayout) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (type == QuimiaToastType.Loading) Box(Modifier.size(if (compactLayout) 24.dp else if (compact) 40.dp else 50.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = tokens.foregroundPrimary, strokeWidth = 2.dp)
            } else QuimiaShapeIcon(
                size = if (compact) 40.dp else QuimiaShapeIconSize.Small.value,
                icon = statusIcon,
                theme = iconTheme,
                containerColor = statusIconContainerColor,
            )
            Column(
                modifier = Modifier.weight(1f).sizeIn(minHeight = if (compactLayout) 24.dp else 50.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = message,
                    color = tokens.foregroundPrimary,
                    style = if (compactLayout) QuimiaTextStyles.medium14 else QuimiaTextStyles.medium16,
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

            if (canDismiss) {
                IconButton(onClick = requestDismiss, modifier = Modifier.size(40.dp)) {
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
    description: String? = null, onDismiss: (() -> Unit)? = null, compactLayout: Boolean = false) {
    val visibility = remember { MutableTransitionState(false) }
    var retained by remember { mutableStateOf(message) }
    var retainedType by remember { mutableStateOf(type) }
    var retainedDescription by remember { mutableStateOf(description) }
    val motion = LocalQuimiaMotionEnabled.current
    LaunchedEffect(message, type, description) {
        if (message != null) { retained = message; retainedType = type; retainedDescription = description }
        visibility.targetState = message != null
    }
    AnimatedVisibility(visibleState = visibility, modifier = modifier,
        enter = fadeIn(QuimiaMotion.tweenSpec(motion, 180)) + slideInVertically(QuimiaMotion.tweenSpec(motion, 220)) { -it / 5 },
        exit = fadeOut(QuimiaMotion.tweenSpec(motion, 180)) + shrinkVertically(QuimiaMotion.tweenSpec(motion, 220), shrinkTowards = Alignment.Top)) {
        retained?.let { QuimiaToast(it, type = retainedType, description = retainedDescription,
            onDismiss = onDismiss, animateEntrance = false, compactLayout = compactLayout) }
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
