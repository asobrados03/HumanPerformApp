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

    @Test
    fun loadStatistics_whenUserIdInvalid_emitsError() = runTest {
        val viewModel = UserStatsViewModel(
            UserStatsUseCase(FakeUserStatsRepository(Result.success(UserStatistics())))
        )

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(0)
            assertEquals(UserStatsState.Error("ID de usuario inválido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStatistics_whenSuccess_emitsLoadingThenSuccess() = runTest {
        val stats = UserStatistics(lastMonthWorkouts = 12, mostFrequentTrainer = "Ana", pendingBookings = 2)
        val viewModel = UserStatsViewModel(
            UserStatsUseCase(FakeUserStatsRepository(Result.success(stats)))
        )

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Loading, awaitItem())
            assertEquals(UserStatsState.Success(stats), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStatistics_whenFailureWithoutMessage_emitsUnknownError() = runTest {
        val viewModel = UserStatsViewModel(
            UserStatsUseCase(FakeUserStatsRepository(Result.failure(IllegalStateException())))
        )

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Loading, awaitItem())
            assertEquals(UserStatsState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
