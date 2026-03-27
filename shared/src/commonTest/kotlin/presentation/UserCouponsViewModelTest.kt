package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import com.humanperformcenter.shared.presentation.viewmodel.UserCouponsViewModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UserCouponsViewModelTest {

    private class FakeUserCouponsRepository(
        private val addResult: Result<Unit> = Result.success(Unit),
        private val couponsResult: Result<List<Coupon>> = Result.success(emptyList())
    ) : UserCouponsRepository {
        override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> = addResult
        override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = couponsResult
    }

    private val coupon = Coupon(
        id = 1,
        code = "WELCOME10",
        discount = 10.0,
        isPercentage = true,
        expiryDate = LocalDate.parse("2026-12-31"),
        productIds = listOf(11)
    )

    @Test
    fun loadUserCoupons_whenSuccess_updatesListAndStopsLoading() = runTest {
        val viewModel = UserCouponsViewModel(
            UserCouponUseCase(FakeUserCouponsRepository(couponsResult = Result.success(listOf(coupon))))
        )

        viewModel.couponUiState.test {
            assertEquals(false, awaitItem().isLoading)
            viewModel.loadUserCoupons(1)

            assertEquals(true, awaitItem().isLoading)
            val success = awaitItem()
            assertEquals(false, success.isLoading)
            assertEquals(listOf(coupon), success.currentCoupons)
            assertEquals(null, success.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadUserCoupons_whenFailure_setsError() = runTest {
        val viewModel = UserCouponsViewModel(
            UserCouponUseCase(
                FakeUserCouponsRepository(
                    couponsResult = Result.failure(IllegalStateException("No disponible"))
                )
            )
        )

        viewModel.couponUiState.test {
            assertEquals(false, awaitItem().isLoading)
            viewModel.loadUserCoupons(1)

            assertEquals(true, awaitItem().isLoading)
            val failed = awaitItem()
            assertEquals(false, failed.isLoading)
            assertEquals("No disponible", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCouponCodeChanged_updatesCodeAndClearsError() {
        val viewModel = UserCouponsViewModel(UserCouponUseCase(FakeUserCouponsRepository()))

        viewModel.onCouponCodeChanged("SPRING20")

        val state = viewModel.couponUiState.value
        assertEquals("SPRING20", state.code)
        assertEquals(null, state.error)
    }

    @Test
    fun addCouponToUser_whenSuccess_refreshesCouponsAndClearsCode() = runTest {
        val viewModel = UserCouponsViewModel(
            UserCouponUseCase(
                FakeUserCouponsRepository(
                    addResult = Result.success(Unit),
                    couponsResult = Result.success(listOf(coupon))
                )
            )
        )
        viewModel.onCouponCodeChanged("WELCOME10")

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "WELCOME10")

            assertEquals(true, awaitItem().isLoading)
            val success = awaitItem()
            assertEquals(false, success.isLoading)
            assertEquals(listOf(coupon), success.currentCoupons)
            assertEquals("", success.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addCouponToUser_whenAddFails_setsError() = runTest {
        val viewModel = UserCouponsViewModel(
            UserCouponUseCase(
                FakeUserCouponsRepository(addResult = Result.failure(IllegalStateException("Cupón inválido")))
            )
        )

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "BAD")

            assertEquals(true, awaitItem().isLoading)
            val failed = awaitItem()
            assertEquals(false, failed.isLoading)
            assertEquals("Cupón inválido", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addCouponToUser_whenRefreshFails_setsError() = runTest {
        val viewModel = UserCouponsViewModel(
            UserCouponUseCase(
                FakeUserCouponsRepository(
                    addResult = Result.success(Unit),
                    couponsResult = Result.failure(IllegalStateException("No se pudo refrescar"))
                )
            )
        )

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "WELCOME10")

            assertEquals(true, awaitItem().isLoading)
            val failed = awaitItem()
            assertEquals(false, failed.isLoading)
            assertEquals("No se pudo refrescar", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
