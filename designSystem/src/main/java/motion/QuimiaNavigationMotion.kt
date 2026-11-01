package motion

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*

/** Shared by both destinations during a dissolve: a gesture never mounts intermediate pages. */
class QuimiaNavigationDragState {
    var dragging by mutableStateOf(false)
        private set
    internal var active by mutableStateOf(false)
    internal var revision by mutableIntStateOf(0)
    var position by mutableFloatStateOf(0f)
        private set
    var direction by mutableFloatStateOf(1f)
        private set
    fun begin(value: Float) { position = value; active = true; dragging = true }
    fun move(value: Float) {
        if (!dragging) return
        if (kotlin.math.abs(value - position) > .002f) direction = if (value > position) 1f else -1f
        position = value
    }
    fun finish() { if (dragging) { dragging = false; revision++ } }
}

data class QuimiaNavigationProgress(val target: Int, val position: State<Float>, val drag: QuimiaNavigationDragState)

val LocalQuimiaNavigationProgress = staticCompositionLocalOf<QuimiaNavigationProgress?> { null }

/** Lives above destinations so the menu indicator keeps its position across screen changes. */
@Composable
internal fun rememberQuimiaNavigationProgress(selectedIndex: Int): QuimiaNavigationProgress {
    val enabled = LocalQuimiaMotionEnabled.current
    val animation = remember { Animatable(selectedIndex.toFloat()) }
    val drag = remember { QuimiaNavigationDragState() }
    LaunchedEffect(selectedIndex, drag.dragging, drag.revision, enabled) {
        if (!drag.dragging) {
            if (drag.active) { animation.snapTo(drag.position); drag.active = false }
            animation.animateTo(selectedIndex.toFloat(), QuimiaMotion.settleSpec(enabled))
        }
    }
    val position = remember { derivedStateOf { if (drag.active) drag.position else animation.value } }
    return QuimiaNavigationProgress(selectedIndex, position, drag)
}

@Composable
fun QuimiaNavigationMotion(selectedIndex: Int, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalQuimiaNavigationProgress provides rememberQuimiaNavigationProgress(selectedIndex), content = content)
}
