package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository

class UserCouponUseCase(
    private val userCouponsRepository: UserCouponsRepository,
) {
    suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> {
        return userCouponsRepository.addCouponToUser(userId, couponCode)
    }

    suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> {
        return userCouponsRepository.getUserCoupons(userId)
    }
}
