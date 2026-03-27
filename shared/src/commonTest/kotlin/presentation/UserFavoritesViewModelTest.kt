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
        private val coachesResult: Result<List<Professional>> = Result.success(emptyList()),
        private val markFavoriteResult: Result<String> = Result.success("ok"),
        private val preferredCoachResult: Result<GetPreferredCoachResponse> = Result.success(GetPreferredCoachResponse(1))
    ) : UserFavoritesRepository {
        override suspend fun getCoaches(): Result<List<Professional>> = coachesResult
        override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> = markFavoriteResult
        override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> = preferredCoachResult
    }

    private fun buildViewModel(repository: FakeUserFavoritesRepository = FakeUserFavoritesRepository()) =
        UserFavoritesViewModel(UserCoachesUseCase(repository))

    @Test
    fun getcoaches_when_success_emits_loading_then_success() = runTest {
        val coaches = listOf(Professional(id = 1, name = "Ana"))
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(coachesResult = Result.success(coaches))
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
    fun markfavorite_when_failure_without_message_emits_fallback_error_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(markFavoriteResult = Result.failure(IllegalStateException()))
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
    fun getpreferredcoach_when_userid_is_null_keeps_idle() {
        val viewModel = buildViewModel()

        viewModel.getPreferredCoach(null)

        assertEquals(GetPreferredCoachState.Idle, viewModel.getPreferredCoachState.value)
    }

    @Test
    fun getpreferredcoach_when_success_emits_loading_then_success_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(
            FakeUserFavoritesRepository(preferredCoachResult = Result.success(GetPreferredCoachResponse(7)))
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
