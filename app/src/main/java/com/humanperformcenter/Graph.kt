package com.humanperformcenter

import android.content.Context
import androidx.room.Room
import com.humanperformcenter.data.BudgetRepository
import com.humanperformcenter.data.CategoryRepository
import com.humanperformcenter.data.FinancialDatabase
import com.humanperformcenter.data.TransactionRepository

object Graph {
    private lateinit var database: FinancialDatabase

    val transactionRepository by lazy {
        TransactionRepository(transactionDao = database.transactionDao())
    }

    val categoryRepository by lazy {
        CategoryRepository(categoryDao = database.categoryDao())
    }

    val budgetRepository by lazy {
        BudgetRepository(budgetDao = database.budgetDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(
            context,
            FinancialDatabase::class.java,
            "financialapp.db"
        ).build()
    }
}