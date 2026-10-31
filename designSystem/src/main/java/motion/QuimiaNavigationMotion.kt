package motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf

data class QuimiaNavigationProgress(val target: Int, val position: State<Float>)

val LocalQuimiaNavigationProgress = staticCompositionLocalOf<QuimiaNavigationProgress?> { null }

/** Lives above destinations so the menu indicator keeps its position across screen changes. */
@Composable
fun QuimiaNavigationMotion(selectedIndex: Int, content: @Composable () -> Unit) {
    val position = animateFloatAsState(selectedIndex.toFloat(), QuimiaMotion.settleSpec(LocalQuimiaMotionEnabled.current),
        label = "QuimiaNavigationPosition")
    CompositionLocalProvider(LocalQuimiaNavigationProgress provides QuimiaNavigationProgress(selectedIndex, position), content = content)
}
