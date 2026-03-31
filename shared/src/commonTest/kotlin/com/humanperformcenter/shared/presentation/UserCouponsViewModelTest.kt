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

    private fun buildViewModel(repository: FakeUserCouponsRepository) =
        UserCouponsViewModel(UserCouponUseCase(repository))

    private val coupon = Coupon(
        id = 1,
        code = "WELCOME10",
        discount = 10.0,
        isPercentage = true,
        expiryDate = LocalDate.parse("2026-12-31"),
        productIds = listOf(11)
    )

    @Test
    fun loadUserCoupons_when_success_updates_list_and_clears_loading() = runTest {
        val viewModel = buildViewModel(
            FakeUserCouponsRepository(couponsResult = Result.success(listOf(coupon)))
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
    fun loadUserCoupons_when_failure_sets_error() = runTest {
        val viewModel = buildViewModel(
            FakeUserCouponsRepository(couponsResult = Result.failure(IllegalStateException("No disponible")))
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
    fun onCouponCodeChanged_when_called_updates_code_and_clears_error() {
        val viewModel = buildViewModel(FakeUserCouponsRepository())

        viewModel.onCouponCodeChanged("SPRING20")

        val state = viewModel.couponUiState.value
        assertEquals("SPRING20", state.code)
        assertEquals(null, state.error)
    }

    @Test
    fun addCouponToUser_when_add_and_refresh_succeed_clears_code_and_refreshes_coupons() = runTest {
        val viewModel = buildViewModel(
            FakeUserCouponsRepository(
                addResult = Result.success(Unit),
                couponsResult = Result.success(listOf(coupon))
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
    fun addCouponToUser_when_add_fails_sets_error() = runTest {
        val viewModel = buildViewModel(
            FakeUserCouponsRepository(addResult = Result.failure(IllegalStateException("Cupón inválido")))
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
    fun addCouponToUser_when_refresh_fails_sets_refresh_error() = runTest {
        val viewModel = buildViewModel(
            FakeUserCouponsRepository(
                addResult = Result.success(Unit),
                couponsResult = Result.failure(IllegalStateException("No se pudo refrescar"))
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
