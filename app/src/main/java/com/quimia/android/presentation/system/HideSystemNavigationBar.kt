package com.quimia.android.presentation.system

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun HideSystemNavigationBar() {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view) {
        fun hideNavigationBar() {
            val window = view.findWindow() ?: return
            WindowInsetsControllerCompat(window, window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) hideNavigationBar()
        }
        val hideOnAttach = Runnable { hideNavigationBar() }
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)
        view.post(hideOnAttach)

        onDispose {
            view.removeCallbacks(hideOnAttach)
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            }
        }
    }
}

private fun View.findWindow(): Window? {
    var ancestor: ViewParent? = parent
    while (ancestor != null) {
        if (ancestor is DialogWindowProvider) return ancestor.window
        ancestor = ancestor.parent
    }
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext.window
        currentContext = currentContext.baseContext
    }
    return null
}
