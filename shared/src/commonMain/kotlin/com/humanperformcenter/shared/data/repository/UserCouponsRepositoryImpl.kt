package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.remote.UserCouponsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository

class UserCouponsRepositoryImpl(
    private val remote: UserCouponsRemoteDataSource,
) : UserCouponsRepository {
    override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> =
        remote.addCouponToUser(userId, couponCode)

    override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = remote.getUserCoupons(userId)
}
