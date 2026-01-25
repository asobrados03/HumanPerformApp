package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.presentation.ui.models.ServiceUiModel

sealed class ServiceUiState {
    object Loading : ServiceUiState()
    data class Success(val services: List<ServiceUiModel>) : ServiceUiState()
    data class Error(val message: String) : ServiceUiState()
}