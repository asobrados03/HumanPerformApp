package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
import com.humanperformcenter.shared.domain.usecase.UserCoachesUseCase
import com.humanperformcenter.shared.presentation.ui.CoachState
import com.humanperformcenter.shared.presentation.ui.GetPreferredCoachState
import com.humanperformcenter.shared.presentation.ui.MarkFavoriteState
import com.humanperformcenter.shared.presentation.viewmodel.UserFavoritesViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserFavoritesViewModelTest {

    private class FakeUserFavoritesRepository(
        initialCoaches: List<Professional> = emptyList(),
        preferredCoachByUser: Map<Int, Int> = mapOf(1 to 1),
        private val failOnMarkFavoriteWithoutMessage: Boolean = false
    ) : UserFavoritesRepository {
        private val coaches = initialCoaches.toMutableList()
        private val preferredCoach = preferredCoachByUser.toMutableMap()

        override suspend fun getCoaches(): Result<List<Professional>> = Result.success(coaches.toList())

        override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> {
            if (failOnMarkFavoriteWithoutMessage) return Result.failure(IllegalStateException())
            if (userId != null) preferredCoach[userId] = coachId
            return Result.success("ok")
        }

        override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> =
            Result.success(GetPreferredCoachResponse(preferredCoach[customerId] ?: 0))
    }

    private fun buildViewModel(repository: UserFavoritesRepository = FakeUserFavoritesRepository()) =
        UserFavoritesViewModel(UserCoachesUseCase(repository))

    @Test
    fun getCoaches_when_success_emits_loading_then_success() = runTest {
        val coaches = listOf(Professional(id = 1, name = "Ana"))
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(initialCoaches = coaches)
        )

        viewModel.coachesState.test {
            assertEquals(CoachState.Idle, awaitItem())
            viewModel.getCoaches()
            assertEquals(CoachState.Loading, awaitItem())
            assertEquals(CoachState.Success(coaches), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markFavorite_when_failure_without_message_emits_fallback_error_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(failOnMarkFavoriteWithoutMessage = true)
        )

        viewModel.markFavoriteState.test {
            assertEquals(MarkFavoriteState.Idle, awaitItem())
            viewModel.markFavorite(coachId = 1, serviceName = "PT", userId = 3)
            assertEquals(MarkFavoriteState.Loading, awaitItem())
            assertEquals(MarkFavoriteState.Error("Error desconocido al marcar como favorito"), awaitItem())

            viewModel.clearMarkFavoriteState()
            assertEquals(MarkFavoriteState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPreferredCoach_when_userid_is_null_keeps_idle() {
        val viewModel = buildViewModel()

        viewModel.getPreferredCoach(null)

        assertEquals(GetPreferredCoachState.Idle, viewModel.getPreferredCoachState.value)
    }

    @Test
    fun getPreferredCoach_when_success_emits_loading_then_success_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(preferredCoachByUser = mapOf(1 to 7))
        )

        viewModel.getPreferredCoachState.test {
            assertEquals(GetPreferredCoachState.Idle, awaitItem())
            viewModel.getPreferredCoach(1)
            assertEquals(GetPreferredCoachState.Loading, awaitItem())
            assertEquals(GetPreferredCoachState.Success(7), awaitItem())

            viewModel.clearGetPreferredCoachState()
            assertEquals(GetPreferredCoachState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
