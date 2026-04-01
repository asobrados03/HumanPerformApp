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
        seedCoupons: List<Coupon> = emptyList(),
        private val failOnAddWithMessage: String? = null,
        private val failOnLoadWithMessage: String? = null
    ) : UserCouponsRepository {
        private val couponsByUser = mutableMapOf<Int, MutableList<Coupon>>()
        private var nextId = (seedCoupons.maxOfOrNull { it.id } ?: 0) + 1

        init {
            if (seedCoupons.isNotEmpty()) {
                couponsByUser[1] = seedCoupons.toMutableList()
            }
        }

        override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> {
            failOnAddWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            val coupons = couponsByUser.getOrPut(userId) { mutableListOf() }
            coupons += Coupon(
                id = nextId++,
                code = couponCode,
                discount = 10.0,
                isPercentage = true,
                expiryDate = LocalDate.parse("2026-12-31"),
                productIds = listOf(11)
            )
            return Result.success(Unit)
        }

        override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> {
            failOnLoadWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            return Result.success(couponsByUser[userId].orEmpty().toList())
        }
    }

    private fun buildViewModel(repository: UserCouponsRepository) =
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
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeUserCouponsRepository(seedCoupons = listOf(coupon))
        )

        viewModel.couponUiState.test {
        // Assert
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
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeUserCouponsRepository(failOnLoadWithMessage = "No disponible")
        )

        viewModel.couponUiState.test {
        // Assert
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
        // Arrange
        val viewModel = buildViewModel(FakeUserCouponsRepository())

        // Act
        viewModel.onCouponCodeChanged("SPRING20")

        val state = viewModel.couponUiState.value
        // Assert
        assertEquals("SPRING20", state.code)
        assertEquals(null, state.error)
    }

    @Test
    fun addCouponToUser_when_add_and_refresh_succeed_clears_code_and_refreshes_coupons() = runTest {
        // Arrange
        val viewModel = buildViewModel(FakeUserCouponsRepository())
        // Act
        viewModel.onCouponCodeChanged("WELCOME10")

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "WELCOME10")

        // Assert
            assertEquals(true, awaitItem().isLoading)
            val success = awaitItem()
            assertEquals(false, success.isLoading)
            assertEquals("WELCOME10", success.currentCoupons.first().code)
            assertEquals(1, success.currentCoupons.size)
            assertEquals("", success.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addCouponToUser_when_add_fails_sets_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeUserCouponsRepository(failOnAddWithMessage = "Cupón inválido")
        )

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "BAD")

        // Assert
            assertEquals(true, awaitItem().isLoading)
            val failed = awaitItem()
            assertEquals(false, failed.isLoading)
            assertEquals("Cupón inválido", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addCouponToUser_when_refresh_fails_sets_refresh_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeUserCouponsRepository(failOnLoadWithMessage = "No se pudo refrescar")
        )

        viewModel.couponUiState.test {
            awaitItem()
            viewModel.addCouponToUser(1, "WELCOME10")

        // Assert
            assertEquals(true, awaitItem().isLoading)
            val failed = awaitItem()
            assertEquals(false, failed.isLoading)
            assertEquals("No se pudo refrescar", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
