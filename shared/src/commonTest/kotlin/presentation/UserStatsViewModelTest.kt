package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
import com.humanperformcenter.shared.domain.usecase.UserStatsUseCase
import com.humanperformcenter.shared.presentation.ui.UserStatsState
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserStatsViewModelTest {

    private class FakeUserStatsRepository(
        private val statsResult: Result<UserStatistics>
    ) : UserStatsRepository {
        override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = statsResult
    }

    private fun buildViewModel(statsResult: Result<UserStatistics>) =
        UserStatsViewModel(UserStatsUseCase(FakeUserStatsRepository(statsResult)))

    @Test
    fun loadstatistics_when_invalid_user_id_emits_error() = runTest {
        val viewModel = buildViewModel(Result.success(UserStatistics()))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(0)
            assertEquals(UserStatsState.Error("ID de usuario inválido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadstatistics_when_success_emits_loading_then_success() = runTest {
        val stats = UserStatistics(lastMonthWorkouts = 12, mostFrequentTrainer = "Ana", pendingBookings = 2)
        val viewModel = buildViewModel(Result.success(stats))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Success(stats), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadstatistics_when_failure_without_message_emits_unknown_error() = runTest {
        val viewModel = buildViewModel(Result.failure(IllegalStateException()))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
