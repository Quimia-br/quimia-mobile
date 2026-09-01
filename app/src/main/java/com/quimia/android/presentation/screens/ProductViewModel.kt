package com.quimia.android.presentation.screens

import androidx.lifecycle.viewModelScope
import com.quimia.android.data.model.ProductResponse
import com.quimia.android.data.remote.ApiResult
import com.quimia.android.data.repository.ProductRepository
import com.quimia.android.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

class ProductViewModel(cosmosToken: String) : BaseViewModel<ProductResponse>() {
    private val repository = ProductRepository(cosmosToken)

    fun searchByBarcode(barcode: String) {
        _state.value = ApiResult.Loading
        launchAsync {
            val result = repository.searchByBarcode(barcode)
            _state.value = result
        }
    }

    fun retrySearch(barcode: String) {
        searchByBarcode(barcode)
    }
}
