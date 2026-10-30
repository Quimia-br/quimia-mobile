package motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import theme.LocalQuimiaSkeleton

fun Modifier.quimiaPress(pressed: Boolean, pressedScale: Float = QuimiaMotion.PressScale): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val scale = animateFloatAsState(if (pressed && enabled) pressedScale else 1f,
        if (pressed) QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Quick) else QuimiaMotion.settleSpec(enabled), label = "press")
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}

/** Uses the same interaction for touch feedback and click semantics. No separate gesture detector. */
fun Modifier.quimiaClickable(enabled: Boolean = true, role: Role? = null, onClickLabel: String? = null, onClick: () -> Unit): Modifier = composed {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    quimiaPress(pressed, QuimiaMotion.CardPressScale).clickable(enabled = enabled, role = role, onClickLabel = onClickLabel,
        interactionSource = interactions, indication = LocalIndication.current, onClick = onClick)
}

/** Feedback for a newly selected favorite; existing selections never pulse on screen entry. */
fun Modifier.quimiaSelectionFeedback(selected: Boolean): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    var previous by remember { mutableStateOf(selected) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected, enabled) {
        val newlySelected = selected && !previous
        previous = selected
        if (enabled && newlySelected) {
            scale.animateTo(1.12f, QuimiaMotion.tweenSpec(true, QuimiaMotion.Quick))
            scale.animateTo(1f, QuimiaMotion.settleSpec(true))
        } else scale.snapTo(1f)
    }
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}

/** Replays only on a semantic change (navigation, skeleton becoming data, or feedback). */
fun Modifier.quimiaEnter(key: Any? = Unit, emphasis: Boolean = false): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val progress = remember(key) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key, enabled) {
        if (enabled) progress.animateTo(1f, QuimiaMotion.tweenSpec(true, QuimiaMotion.Entrance))
        else progress.snapTo(1f)
    }
    graphicsLayer {
        alpha = progress.value
        translationY = if (emphasis) 0f else (1f - progress.value) * 4.dp.toPx()
        val scale = if (emphasis) QuimiaMotion.EmphasisScale + (1f - QuimiaMotion.EmphasisScale) * progress.value else 1f
        scaleX = scale; scaleY = scale
    }
}

fun Modifier.quimiaResize(): Modifier = composed {
    animateContentSize(QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current, QuimiaMotion.Expansion))
}

@Composable
fun quimiaAnimatedColor(target: Color): Color = animateColorAsState(target,
    QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "selectionColor").value

/** Body-only transition: content remains visible and navigation never belongs to this layer. */
fun Modifier.quimiaContent(key: Any? = Unit): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val progress = remember(key, enabled) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key, enabled) {
        if (enabled) progress.animateTo(1f, QuimiaMotion.tweenSpec(true, QuimiaMotion.Entrance)) else progress.snapTo(1f)
    }
    graphicsLayer {
        alpha = .92f + .08f * progress.value
        translationY = (1f - progress.value) * 8.dp.toPx()
    }
}

fun Modifier.quimiaRotation(degrees: Float): Modifier = composed {
    val angle = animateFloatAsState(degrees, QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "disclosure")
    graphicsLayer { rotationZ = angle.value }
}
