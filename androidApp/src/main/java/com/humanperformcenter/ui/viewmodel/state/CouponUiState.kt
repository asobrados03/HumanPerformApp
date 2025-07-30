package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.Coupon

data class CouponUiState(
    val code: String?       = "",
    val isLoading: Boolean  = false,
    val error: String?      = null,
    val currentCoupon: Coupon? = null
)