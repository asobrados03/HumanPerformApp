package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction

interface UserWalletRemoteDataSource {
    suspend fun getEwalletBalance(userId: Int): Result<Double?>
    suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>>
}
