package com.quimia.android.presentation.components

/** A visible menu always wins over delayed loading, including a stale authentication flag. */
internal fun brandedLoadingVisible(
    navigationVisible: Boolean,
    accountLoading: Boolean,
    aiEntry: Boolean,
    pending: Boolean,
    delayElapsed: Boolean,
): Boolean = !navigationVisible && (accountLoading || aiEntry || (pending && delayElapsed))
