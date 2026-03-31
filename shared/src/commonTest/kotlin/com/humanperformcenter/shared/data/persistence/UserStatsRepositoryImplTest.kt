package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.data.remote.UserStatsRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserStatsRepositoryImplTest {

    @Test
    fun get_user_stats_when_success_propagates_expected_model() = runTest {
        val expected = UserStatistics(lastMonthWorkouts = 12, mostFrequentTrainer = "Mario", pendingBookings = 1)
        val repository = UserStatsRepositoryImpl(FakeUserStatsRemoteDataSource(Result.success(expected)))

        val result = repository.getUserStats(customerId = 4)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun get_user_stats_when_remote_error_maps_to_domain_network() = runTest {
        val repository = UserStatsRepositoryImpl(
            FakeUserStatsRemoteDataSource(Result.failure(IOException("No network"))),
        )

        val result = repository.getUserStats(customerId = 4)

        assertTrue(result.isFailure)
        assertIs<DomainException.Network>(result.exceptionOrNull())
    }

    @Test
    fun get_user_stats_when_remote_returns_empty_defaults_propagates_model() = runTest {
        val expected = UserStatistics()
        val repository = UserStatsRepositoryImpl(FakeUserStatsRemoteDataSource(Result.success(expected)))

        val result = repository.getUserStats(customerId = 8)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun get_user_stats_when_contract_edge_inconsistent_negative_values_are_not_mutated() = runTest {
        val expected = UserStatistics(lastMonthWorkouts = -3, mostFrequentTrainer = null, pendingBookings = -1)
        val repository = UserStatsRepositoryImpl(FakeUserStatsRemoteDataSource(Result.success(expected)))

        val result = repository.getUserStats(customerId = 8)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    private class FakeUserStatsRemoteDataSource(
        private val getUserStatsResult: Result<UserStatistics>,
    ) : UserStatsRemoteDataSource {
        override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = getUserStatsResult
    }
}
