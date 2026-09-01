package com.quimia.android.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quimia.android.data.remote.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T> : ViewModel() {
    protected val _state = MutableStateFlow<ApiResult<T>>(ApiResult.Loading)
    val state: StateFlow<ApiResult<T>> = _state

    protected fun <R> executeAsync(
        onExecute: suspend () -> ApiResult<R>,
        onResult: (ApiResult<R>) -> Unit
    ) {
        viewModelScope.launch {
            val result = onExecute()
            onResult(result)
        }
    }

    protected fun launchAsync(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}
