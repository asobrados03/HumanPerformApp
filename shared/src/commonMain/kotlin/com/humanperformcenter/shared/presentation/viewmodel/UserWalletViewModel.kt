package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.shared.presentation.ui.WalletBalanceUiState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserWalletViewModel(
    private val walletUseCase: WalletUseCase
) : ViewModel() {
    private val _walletBalanceUiState = MutableStateFlow<WalletBalanceUiState>(viewModelScope, WalletBalanceUiState.Idle)
    @NativeCoroutinesState
    val walletBalanceUiState: StateFlow<WalletBalanceUiState> = _walletBalanceUiState.asStateFlow()

    private val _eWalletTransactions = MutableStateFlow<EwalletUiState>(viewModelScope, EwalletUiState.Loading)
    @NativeCoroutinesState
    val eWalletTransactions: StateFlow<EwalletUiState> = _eWalletTransactions.asStateFlow()

    fun loadBalance(userId: Int) {
        if (userId == -1) {
            _walletBalanceUiState.value = WalletBalanceUiState.Error("Usuario inválido")
            return
        }

        viewModelScope.launch {
            _walletBalanceUiState.value = WalletBalanceUiState.Loading

            walletUseCase.getEwalletBalance(userId).fold(
                onSuccess = { newBalance ->
                    _walletBalanceUiState.value = WalletBalanceUiState.Success(newBalance ?: 0.0)
                },
                onFailure = { error ->
                    _walletBalanceUiState.value = WalletBalanceUiState.Error(
                        error.message ?: "No se pudo cargar el saldo"
                    )
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
