package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.Coupon

interface UserCouponsRepository {
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit>
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>>
}
