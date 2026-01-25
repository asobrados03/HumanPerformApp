package com.humanperformcenter.shared.presentation.ui

sealed class CouponEvent {
    data object Success : CouponEvent()
    data class Error(val message: String) : CouponEvent()
}