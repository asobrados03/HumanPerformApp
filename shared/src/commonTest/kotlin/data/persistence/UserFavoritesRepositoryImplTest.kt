package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.remote.UserFavoritesRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserFavoritesRepositoryImplTest {

    @Test
    fun get_coaches_when_success_propagates_expected_models() = runTest {
        val expected = listOf(Professional(id = 3, name = "Lucía", photoName = "l.jpg", service = "Pilates"))
        val repository = UserFavoritesRepositoryImpl(
            FakeUserFavoritesRemoteDataSource(getCoachesResult = Result.success(expected)),
        )

        val result = repository.getCoaches()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun get_preferred_coach_when_remote_error_maps_to_domain_not_found() = runTest {
        val repository = UserFavoritesRepositoryImpl(
            FakeUserFavoritesRemoteDataSource(
                getPreferredCoachResult = Result.failure(IllegalStateException("HTTP 404 Not Found")),
            ),
        )

        val result = repository.getPreferredCoach(customerId = 5)

        assertTrue(result.isFailure)
        assertIs<DomainException.NotFound>(result.exceptionOrNull())
    }

    @Test
    fun get_coaches_when_empty_returns_empty_list() = runTest {
        val repository = UserFavoritesRepositoryImpl(
            FakeUserFavoritesRemoteDataSource(getCoachesResult = Result.success(emptyList())),
        )

        val result = repository.getCoaches()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun mark_favorite_when_contract_edge_nullable_fields_are_forwarded() = runTest {
        val remote = FakeUserFavoritesRemoteDataSource(markFavoriteResult = Result.success("ok"))
        val repository = UserFavoritesRepositoryImpl(remote)

        val result = repository.markFavorite(coachId = 9, serviceName = null, userId = null)

        assertTrue(result.isSuccess)
        assertEquals(null, remote.lastServiceName)
        assertEquals(null, remote.lastUserId)
    }

    private class FakeUserFavoritesRemoteDataSource(
        private val getCoachesResult: Result<List<Professional>> = Result.success(emptyList()),
        private val markFavoriteResult: Result<String> = Result.success("ok"),
        private val getPreferredCoachResult: Result<GetPreferredCoachResponse> = Result.success(GetPreferredCoachResponse(1)),
    ) : UserFavoritesRemoteDataSource {
        var lastServiceName: String? = "sentinel"
        var lastUserId: Int? = -1

        override suspend fun getCoaches(): Result<List<Professional>> = getCoachesResult

        override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> {
            lastServiceName = serviceName
            lastUserId = userId
            return markFavoriteResult
        }

        override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> = getPreferredCoachResult
    }
}
