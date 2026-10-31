package motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import theme.LocalQuimiaSkeleton

/** A small staggered wave only while waiting for an answer; static with reduced motion. */
fun Modifier.quimiaTypingDot(index: Int): Modifier = composed {
    if (!LocalQuimiaMotionEnabled.current || LocalQuimiaSkeleton.current) return@composed this
    val transition = rememberInfiniteTransition(label = "typing")
    val progress by transition.animateFloat(0f, 1f, infiniteRepeatable(
        animation = keyframes {
            durationMillis = 1000
            0f at 0
            1f at 220 using FastOutSlowInEasing
            0f at 440 using FastOutSlowInEasing
            0f at 1000
        }, initialStartOffset = StartOffset(index * 130)), label = "typingDot")
    graphicsLayer { translationY = -3.dp.toPx() * progress; alpha = .45f + .55f * progress }
}
