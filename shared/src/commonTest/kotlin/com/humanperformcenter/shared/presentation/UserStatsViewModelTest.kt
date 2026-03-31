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
        initialStats: Map<Int, UserStatistics> = emptyMap(),
        private val failWithMessage: String? = null,
        private val failWithoutMessage: Boolean = false
    ) : UserStatsRepository {
        private val statsByUser = initialStats.toMutableMap()

        override suspend fun getUserStats(customerId: Int): Result<UserStatistics> {
            if (failWithoutMessage) return Result.failure(IllegalStateException())
            failWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            return statsByUser[customerId]?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Sin estadísticas"))
        }
    }

    private fun buildViewModel(repository: UserStatsRepository) =
        UserStatsViewModel(UserStatsUseCase(repository))

    @Test
    fun loadStatistics_when_invalid_user_id_emits_error() = runTest {
        val viewModel = buildViewModel(FakeUserStatsRepository(initialStats = mapOf(1 to UserStatistics())))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(0)
            assertEquals(UserStatsState.Error("ID de usuario inválido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStatistics_when_success_emits_loading_then_success() = runTest {
        val stats = UserStatistics(lastMonthWorkouts = 12, mostFrequentTrainer = "Ana", pendingBookings = 2)
        val viewModel = buildViewModel(FakeUserStatsRepository(initialStats = mapOf(1 to stats)))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Success(stats), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStatistics_when_failure_without_message_emits_unknown_error() = runTest {
        val viewModel = buildViewModel(FakeUserStatsRepository(failWithoutMessage = true))

        viewModel.uiState.test {
            assertEquals(UserStatsState.Loading, awaitItem())
            viewModel.loadStatistics(1)
            assertEquals(UserStatsState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
