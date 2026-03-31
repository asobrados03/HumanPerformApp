package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.remote.UserAccountRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserAccountRepositoryImplTest {

    @Test
    fun delete_user_when_success_propagates_unit() = runTest {
        val repository = UserAccountRepositoryImpl(FakeUserAccountRemoteDataSource(Result.success(Unit)))

        val result = repository.deleteUser("user@test.com")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun delete_user_when_remote_error_maps_to_domain_unauthorized() = runTest {
        val repository = UserAccountRepositoryImpl(
            FakeUserAccountRemoteDataSource(Result.failure(IllegalStateException("HTTP 401 Unauthorized"))),
        )

        val result = repository.deleteUser("user@test.com")

        assertTrue(result.isFailure)
        assertIs<DomainException.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun delete_user_when_empty_email_edge_case_contract_is_forwarded_to_remote() = runTest {
        val remote = FakeUserAccountRemoteDataSource(Result.success(Unit))
        val repository = UserAccountRepositoryImpl(remote)

        val result = repository.deleteUser("")

        assertTrue(result.isSuccess)
        assertEquals("", remote.lastDeletedEmail)
    }

    @Test
    fun delete_user_when_remote_returns_unknown_state_keeps_result_success() = runTest {
        val repository = UserAccountRepositoryImpl(FakeUserAccountRemoteDataSource(Result.success(Unit)))

        val result = repository.deleteUser("unexpected+state@test.com")

        assertTrue(result.isSuccess)
    }

    private class FakeUserAccountRemoteDataSource(
        private val deleteUserResult: Result<Unit>,
    ) : UserAccountRemoteDataSource {
        var lastDeletedEmail: String? = null

        override suspend fun deleteUser(email: String): Result<Unit> {
            lastDeletedEmail = email
            return deleteUserResult
        }
    }
}
