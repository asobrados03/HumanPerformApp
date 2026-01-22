package com.humanperformcenter.ui.viewmodel.state

import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

sealed class StripeUiState {
    object Idle : StripeUiState()
    data class Ready(val clientSecret: String, val config: PaymentSheet.Configuration) : StripeUiState()
    data class Result(val result: PaymentSheetResult) : StripeUiState()
    data class Error(val message: String) : StripeUiState()
}