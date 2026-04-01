package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserCouponUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun addCouponToUser_whenCodeIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(addResult = Result.success(Unit)))
        assertTrue(useCase.addCouponToUser(1, "WELCOME10").isSuccess)
    }

    @Test
    fun addCouponToUser_whenCodeIsEmpty_returnsFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(addResult = Result.failure(IllegalArgumentException("vacío"))))
        assertTrue(useCase.addCouponToUser(1, "").isFailure)
    }

    @Test
    fun getUserCoupons_whenCouponsExist_returnsList() = runTest {
        val expected = listOf(Coupon(1, "WELCOME10", 10.0, true, LocalDate.parse("2026-12-31"), listOf(5)))
        val useCase = buildUseCase(FakeRepo(getResult = Result.success(expected)))
        assertEquals(expected, useCase.getUserCoupons(1).getOrNull())
    }

    @Test
    fun getUserCoupons_whenRepositoryFails_propagatesFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(getResult = Result.failure(RuntimeException("500"))))
        assertTrue(useCase.getUserCoupons(1).isFailure)
    }

    private fun buildUseCase(repo: UserCouponsRepository): UserCouponUseCase {
        startKoin { modules(module { single<UserCouponsRepository> { repo }; single { UserCouponUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val addResult: Result<Unit> = Result.success(Unit),
        private val getResult: Result<List<Coupon>> = Result.success(emptyList()),
    ) : UserCouponsRepository {
        override suspend fun addCouponToUser(userId: Int, couponCode: String) = addResult
        override suspend fun getUserCoupons(userId: Int) = getResult
    }
}
