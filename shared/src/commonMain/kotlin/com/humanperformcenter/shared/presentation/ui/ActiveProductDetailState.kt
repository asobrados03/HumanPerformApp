package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse

sealed class ActiveProductDetailState {
    object Loading : ActiveProductDetailState()
    data class Success(val product: ProductDetailResponse) : ActiveProductDetailState()
    data class Error(val message: String) : ActiveProductDetailState()
}