package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.Product

sealed class UserProductsUiState {
    object Loading : UserProductsUiState()
    data class Success(val products: List<Product>) : UserProductsUiState()
    data class Error(val message: String) : UserProductsUiState()
}