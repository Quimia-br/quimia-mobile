package theme

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalQuimiaSkeleton = staticCompositionLocalOf { false }
private val LocalSkeletonOpacity = staticCompositionLocalOf<State<Float>?> { null }

/** One pulse per loading page; its value is read during drawing, never by the page composition. */
@Composable
fun QuimiaSkeleton(enabled: Boolean, content: @Composable () -> Unit) {
    val opacity = if (enabled) rememberSkeletonOpacity() else null
    CompositionLocalProvider(LocalQuimiaSkeleton provides enabled, LocalSkeletonOpacity provides opacity, content = content)
}

@Composable
fun rememberSkeletonOpacity(): State<Float> {
    LocalSkeletonOpacity.current?.let { return it }
    return if (motion.LocalQuimiaMotionEnabled.current) {
        rememberInfiniteTransition(label = "skeletonPulse").animateFloat(motion.QuimiaMotion.SkeletonMin, motion.QuimiaMotion.SkeletonMax,
            infiniteRepeatable(tween(motion.QuimiaMotion.SkeletonPeriod, easing = motion.QuimiaMotion.Easing), RepeatMode.Reverse), label = "skeletonOpacity")
    } else rememberUpdatedState(.16f)
}

/** Keep the real component's measured bounds, hiding its data and interactions while loading. */
fun Modifier.quimiaSkeleton(radius: Dp = 24.dp): Modifier = composed {
    if (!LocalQuimiaSkeleton.current) return@composed this
    val alpha = rememberSkeletonOpacity()
    val color = quimiaColorTokens().textPrimary
    this.drawWithContent { drawRoundRect(color.copy(alpha = alpha.value), cornerRadius = CornerRadius(radius.toPx())) }
        .clearAndSetSemantics { stateDescription = "Carregando" }
        .pointerInput(Unit) {
            awaitEachGesture { do { val event = awaitPointerEvent(); event.changes.forEach { it.consume() } } while (event.changes.any { it.pressed }) }
        }
}
