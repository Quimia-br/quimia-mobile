package com.quimia.android.data.project

import kotlinx.coroutines.flow.StateFlow

interface SessionStorage {
    val session: StateFlow<ApiSession?>
    fun save(value: ApiSession)
    fun clear()
}
