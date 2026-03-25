package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserCouponUseCaseTest {
    private class FakeUserCouponsRepository(
        private val couponsResult: Result<List<Coupon>>
    ) : UserCouponsRepository {
        override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> = Result.success(Unit)
        override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = couponsResult
    }

    @Test
    fun userCouponUseCase_whenRepositoryReturnsCoupons_returnsSuccess() = runBlocking {
        // Arrange
        val coupons = listOf(Coupon(1, "WELCOME", 10.0, true, LocalDate(2026, 12, 1), emptyList()))
        val useCase = UserCouponUseCase(FakeUserCouponsRepository(Result.success(coupons)))

        // Act
        val result = useCase.getUserCoupons(10)

        // Assert
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun userCouponUseCase_whenRepositoryReturnsEmptyList_returnsEmptyList() = runBlocking {
        // Arrange
        val useCase = UserCouponUseCase(FakeUserCouponsRepository(Result.success(emptyList())))

        // Act
        val result = useCase.getUserCoupons(10)

        // Assert
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun userCouponUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserCouponUseCase(FakeUserCouponsRepository(Result.failure(IllegalStateException("boom"))))

        // Act
        val result = useCase.getUserCoupons(10)

        // Assert
        assertTrue(result.isFailure)
    }
}
