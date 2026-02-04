package com.humanperformcenter.shared.data.model.payment

sealed class SharedPaymentResult {
    object Completed : SharedPaymentResult()
    object Canceled : SharedPaymentResult()
    data class Failed(val message: String?) : SharedPaymentResult()
}
