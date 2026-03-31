package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.remote.UserCouponsRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserCouponsRepositoryImplTest {

    @Test
    fun get_user_coupons_when_success_propagates_expected_coupons() = runTest {
        val expected = listOf(
            Coupon(
                id = 1,
                code = "WELCOME10",
                discount = 10.0,
                isPercentage = true,
                expiryDate = LocalDate.parse("2026-12-31"),
                productIds = listOf(3, 4),
            ),
        )
        val repository = UserCouponsRepositoryImpl(
            FakeUserCouponsRemoteDataSource(getUserCouponsResult = Result.success(expected)),
        )

        val result = repository.getUserCoupons(1)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun add_coupon_to_user_when_remote_error_maps_to_domain_bad_request() = runTest {
        val repository = UserCouponsRepositoryImpl(
            FakeUserCouponsRemoteDataSource(
                addCouponToUserResult = Result.failure(IllegalStateException("HTTP 400 Bad Request")),
            ),
        )

        val result = repository.addCouponToUser(1, "INVALID")

        assertTrue(result.isFailure)
        assertIs<DomainException.BadRequest>(result.exceptionOrNull())
    }

    @Test
    fun get_user_coupons_when_empty_returns_empty_list() = runTest {
        val repository = UserCouponsRepositoryImpl(
            FakeUserCouponsRemoteDataSource(getUserCouponsResult = Result.success(emptyList())),
        )

        val result = repository.getUserCoupons(2)

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun add_coupon_to_user_when_contract_edge_empty_coupon_code_is_forwarded() = runTest {
        val remote = FakeUserCouponsRemoteDataSource(addCouponToUserResult = Result.success(Unit))
        val repository = UserCouponsRepositoryImpl(remote)

        val result = repository.addCouponToUser(userId = 3, couponCode = "")

        assertTrue(result.isSuccess)
        assertEquals("", remote.lastCouponCode)
    }

    private class FakeUserCouponsRemoteDataSource(
        private val addCouponToUserResult: Result<Unit> = Result.success(Unit),
        private val getUserCouponsResult: Result<List<Coupon>> = Result.success(emptyList()),
    ) : UserCouponsRemoteDataSource {
        var lastCouponCode: String? = null

        override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> {
            lastCouponCode = couponCode
            return addCouponToUserResult
        }

        override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = getUserCouponsResult
    }
}
