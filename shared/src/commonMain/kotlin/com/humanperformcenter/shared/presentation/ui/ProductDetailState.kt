package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: ProductDetailResponse) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}