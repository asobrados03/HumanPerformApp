package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.product_service.ServiceItem

sealed class ServiceProductUiState {
    object Loading : ServiceProductUiState()
    data class Success(val services: List<ServiceItem>) : ServiceProductUiState()
    data class Error(val message: String) : ServiceProductUiState()
}