package motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import theme.LocalQuimiaSkeleton

fun Modifier.quimiaPress(pressed: Boolean): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val scale = animateFloatAsState(if (pressed && enabled) QuimiaMotion.PressScale else 1f,
        QuimiaMotion.tweenSpec(enabled, if (pressed) QuimiaMotion.Quick else QuimiaMotion.Standard), label = "press")
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
    animateContentSize(QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current))
}

@Composable
fun quimiaAnimatedColor(target: Color): Color = animateColorAsState(target,
    QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "selectionColor").value

/** Body-only transition: content remains visible and navigation never belongs to this layer. */
fun Modifier.quimiaContent(key: Any? = Unit): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val progress = remember(key, enabled) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key, enabled) {
        if (enabled) progress.animateTo(1f, QuimiaMotion.tweenSpec(true)) else progress.snapTo(1f)
    }
    graphicsLayer {
        alpha = .90f + .10f * progress.value
        translationY = (1f - progress.value) * 3.dp.toPx()
    }
}

fun Modifier.quimiaRotation(degrees: Float): Modifier = composed {
    val angle = animateFloatAsState(degrees, QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "disclosure")
    graphicsLayer { rotationZ = angle.value }
}
