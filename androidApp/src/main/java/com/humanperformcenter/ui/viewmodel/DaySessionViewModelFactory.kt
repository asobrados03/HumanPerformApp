package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase

class DaySessionViewModelFactory(
    private val useCase: DaySessionUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DaySessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DaySessionViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
