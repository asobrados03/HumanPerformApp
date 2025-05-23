package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.humanperformcenter.shared.domain.usecase.AuthUseCase

class AuthViewModelFactory(
    private val authUseCase: AuthUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(authUseCase) as T
    }
}