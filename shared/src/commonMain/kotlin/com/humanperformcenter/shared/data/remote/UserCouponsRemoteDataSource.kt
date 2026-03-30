package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.payment.Coupon

interface UserCouponsRemoteDataSource {
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit>
    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>>
}
