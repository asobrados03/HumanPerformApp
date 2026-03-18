package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction

interface UserWalletRepository {
    suspend fun getEwalletBalance(userId: Int): Result<Double?>
    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>>
}
