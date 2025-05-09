package com.humanperformcenter.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.addTransaction(transaction)
    }

    fun getTransactions() : Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionById(id:Long): Flow<Transaction>{
        return transactionDao.getTransactionById(id)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionCountForCategory(categoryId: Long): Int {
        return transactionDao.getTransactionCountForCategory(categoryId)
    }

    fun getTransactionsByCategoryAndDate(categoryId: Long, year: Int, month: String): Flow<List<Transaction>> {
        val yearString = year.toString()

        return transactionDao.getTransactionsByCategoryAndDate(categoryId, yearString, month)
    }
}