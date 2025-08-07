package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase

class PaymentViewModelFactory(
    private val useCaseGoogle: GooglePayUseCase,
    private val useCase: PaymentUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(useCase, useCaseGoogle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
