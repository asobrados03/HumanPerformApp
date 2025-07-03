package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase

class ServiceProductViewModelFactory(
    private val useCase: ServiceProductUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceProductViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
