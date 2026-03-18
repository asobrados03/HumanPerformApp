package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.domain.repository.UserWalletRepository

class WalletUseCase(
    private val userWalletRepository: UserWalletRepository,
) {
    suspend fun getEwalletBalance(userId: Int): Result<Double?> {
        return userWalletRepository.getEwalletBalance(userId)
    }

    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> {
        return userWalletRepository.getEwalletTransactions(userId)
    }
}
