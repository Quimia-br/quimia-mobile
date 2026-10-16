package progressBar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import theme.QuimiaTheme
import theme.quimiaColorTokens

@Composable
fun QuimiaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    progressColor: Color? = null,
    trackColor: Color? = null,
    animationDurationMillis: Int = 250,
) {
    val tokens = quimiaColorTokens()
    val targetProgress = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = animationDurationMillis.coerceAtLeast(0)),
        label = "QuimiaProgress",
    )
    val resolvedProgressColor = progressColor ?: tokens.brandAccent
    val resolvedTrackColor = trackColor ?: tokens.secondary
    val layoutDirection = LocalLayoutDirection.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.coerceAtLeast(1.dp))
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(targetProgress, 0f..1f)
            },
    ) {
        val radius = size.height / 2f
        drawRoundRect(
            color = resolvedTrackColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
        )

        val progressWidth = size.width * animatedProgress
        if (progressWidth > 0f) {
            drawRoundRect(
                color = resolvedProgressColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr) 0f else size.width - progressWidth,
                    y = 0f,
                ),
                size = androidx.compose.ui.geometry.Size(progressWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6, widthDp = 400)
@Composable
private fun QuimiaProgressBarPreview() {
    QuimiaTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            listOf(0.1f, 0.5f, 0.8f, 1f).forEach { progress ->
                QuimiaProgressBar(progress = progress)
            }
        }
    }
}
