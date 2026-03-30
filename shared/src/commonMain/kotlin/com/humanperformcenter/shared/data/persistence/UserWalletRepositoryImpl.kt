package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.remote.UserWalletRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserWalletRepository

class UserWalletRepositoryImpl(
    private val remote: UserWalletRemoteDataSource,
) : UserWalletRepository {
    override suspend fun getEwalletBalance(userId: Int): Result<Double?> = remote.getEwalletBalance(userId).mapDomainError()
    override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> =
        remote.getEwalletTransactions(userId).mapDomainError()
}
