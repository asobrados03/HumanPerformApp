package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserWalletViewModel(
    private val walletUseCase: WalletUseCase
) : ViewModel() {
    private val _balance = MutableStateFlow<Double?>(0.0)
    @NativeCoroutinesState
    val balance: StateFlow<Double?> = _balance

    private val _eWalletTransactions = MutableStateFlow<EwalletUiState>(EwalletUiState.Loading)
    @NativeCoroutinesState
    val eWalletTransactions: StateFlow<EwalletUiState> = _eWalletTransactions.asStateFlow()

    fun loadBalance(userId: Int) {
        if (userId == -1) {
            _balance.value = 0.0
            return
        }

        viewModelScope.launch {
            walletUseCase.getEwalletBalance(userId).fold(
                onSuccess = { nuevoBalance ->
                    _balance.value = nuevoBalance ?: 0.0
                },
                onFailure = { error ->
                    println("❌ Error al cargar el balance: ${error.message}")
                }
            )
        }
    }

    fun loadEwalletTransactions(userId: Int) {
        viewModelScope.launch {
            _eWalletTransactions.value = EwalletUiState.Loading

            walletUseCase.getEwalletTransactions(userId)
                .onSuccess { list ->
                    _eWalletTransactions.value = EwalletUiState.Success(list)
                }
                .onFailure { e ->
                    _eWalletTransactions.value = EwalletUiState.Error(
                        e.message ?: "Error desconocido"
                    )
                }
        }
    }
}
