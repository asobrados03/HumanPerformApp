package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.ServiceItem

sealed class UserProductsUiState {
    object Loading : UserProductsUiState()
    data class Success(val products: List<ServiceItem>) : UserProductsUiState()
    data class Error(val message: String) : UserProductsUiState()
}