package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.Product

sealed class ProductDetailUiState {
    data object Idle : ProductDetailUiState()
    data object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}
