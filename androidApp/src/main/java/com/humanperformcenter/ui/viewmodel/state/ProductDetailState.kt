package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.ProductDetailResponse

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: ProductDetailResponse) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}