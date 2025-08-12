package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase

class PaymentViewModelFactory(
    private val useCaseGoogle: GooglePayUseCase,
    private val useCase: PaymentUseCase,
    private val useCaseStripe: StripeUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(useCase, useCaseGoogle,useCaseStripe) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
