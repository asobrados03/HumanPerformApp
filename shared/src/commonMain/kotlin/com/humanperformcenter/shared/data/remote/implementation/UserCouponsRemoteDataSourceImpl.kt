package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserCouponsRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserCouponsRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserCouponsRemoteDataSource {
    override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/mobile/users/$userId/coupons") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("coupon_code" to couponCode))
        }
    }

    override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/users/$userId/coupons").body()
    }
}
