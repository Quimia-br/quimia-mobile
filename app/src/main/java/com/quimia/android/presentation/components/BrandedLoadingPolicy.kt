package com.quimia.android.presentation.components

/** Tab changes use skeletons. Internal destinations show loading in their first pending frame. */
internal fun brandedLoadingVisible(
    navigationVisible: Boolean,
    accountLoading: Boolean,
    aiEntry: Boolean,
    pending: Boolean,
): Boolean = !navigationVisible && (accountLoading || aiEntry || pending)
