package com.humanperformcenter.shared.presentation.ui

data class AddPaymentMethodSheetData(
    val setupIntentClientSecret: String,
    val ephemeralKeySecret: String,
    val customerId: String,
    val publishableKey: String,
    val merchantDisplayName: String = "HumanPerformCenter"
)

sealed class AddPaymentMethodUiState {
    object Idle : AddPaymentMethodUiState()
    object Loading : AddPaymentMethodUiState()
    data class Ready(val sheetData: AddPaymentMethodSheetData) : AddPaymentMethodUiState()
    object Completed : AddPaymentMethodUiState()
    object Canceled : AddPaymentMethodUiState()
    data class Failed(val message: String) : AddPaymentMethodUiState()
}
