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
        QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Quick), label = "press")
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}

/** Replays only on a semantic change (navigation, skeleton becoming data, or feedback). */
fun Modifier.quimiaEnter(key: Any? = Unit, index: Int = 0, emphasis: Boolean = false): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    val progress = remember(key, enabled) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key, enabled) {
        if (enabled) progress.animateTo(1f, QuimiaMotion.tweenSpec(true, QuimiaMotion.Entrance,
            (index.coerceAtLeast(0) * QuimiaMotion.Stagger).coerceAtMost(QuimiaMotion.MaxStagger)))
        else progress.snapTo(1f)
    }
    graphicsLayer {
        alpha = progress.value
        translationY = if (emphasis) 0f else (1f - progress.value) * 10.dp.toPx()
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

fun Modifier.quimiaSelected(selected: Boolean): Modifier = composed {
    val scale = animateFloatAsState(if (selected) 1.06f else 1f,
        QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "selectedIcon")
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}

fun Modifier.quimiaRotation(degrees: Float): Modifier = composed {
    val angle = animateFloatAsState(degrees, QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current), label = "disclosure")
    graphicsLayer { rotationZ = angle.value }
}
