package com.humanperformcenter.ui.viewmodel.state

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val token: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}
