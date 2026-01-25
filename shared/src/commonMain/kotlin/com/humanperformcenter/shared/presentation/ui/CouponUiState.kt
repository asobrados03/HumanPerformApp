package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.Coupon

data class CouponUiState(
    val code: String?       = "",
    val isLoading: Boolean  = false,
    val error: String?      = null,
    val currentCoupons: List<Coupon> = emptyList()
)