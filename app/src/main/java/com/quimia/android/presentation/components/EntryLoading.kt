package com.quimia.android.presentation.components

import androidx.compose.runtime.*

/** A page may use its full skeleton only until its first request batch settles. */
internal class EntryLoadingGate {
    private var settled = false
    fun isLoading(pending: Boolean): Boolean = pending && !settled
    fun settle(pending: Boolean) { if (!pending) settled = true }
}

@Composable
internal fun rememberEntryLoading(pending: Boolean, key: Any?): Boolean {
    val gate = remember(key) { EntryLoadingGate() }
    val loading = gate.isLoading(pending)
    SideEffect { gate.settle(pending) }
    return loading
}
