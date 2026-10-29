package motion

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

val LocalQuimiaMotionEnabled = staticCompositionLocalOf { true }

@Composable
fun QuimiaMotionTheme(content: @Composable () -> Unit) {
    if (LocalInspectionMode.current) {
        CompositionLocalProvider(LocalQuimiaMotionEnabled provides false, content = content)
        return
    }
    val resolver = LocalContext.current.contentResolver
    fun readEnabled() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var enabled by remember(resolver) { mutableStateOf(readEnabled()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { enabled = readEnabled() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    CompositionLocalProvider(LocalQuimiaMotionEnabled provides enabled, content = content)
}
